package hashmap.api.cache;

import hashmap.api.model.Conteudo;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConteudoCache {

    private static final Logger log = LoggerFactory.getLogger(ConteudoCache.class);

    private final ConcurrentHashMap<Long, EntradaCache> itens = new ConcurrentHashMap<>();
    private final int limite;
    private final long ttlMillis;
    private final ThreadLocal<Boolean> ultimoHit = ThreadLocal.withInitial(() -> false);

    private long hits;
    private long misses;
    private long evictions;

    public ConteudoCache(
            @Value("${app.cache.max-itens:5}") int limite,
            @Value("${app.cache.ttl-segundos:60}") long ttlSegundos) {
        if (limite < 1) {
            throw new IllegalArgumentException("app.cache.max-itens deve ser pelo menos 1");
        }
        this.limite = limite;
        this.ttlMillis = TimeUnit.SECONDS.toMillis(ttlSegundos);
    }

    public synchronized Conteudo buscar(Long id) {
        long agora = System.currentTimeMillis();
        EntradaCache entrada = itens.get(id);

        if (entrada == null || agora - entrada.armazenadoEm >= ttlMillis) {
            if (entrada != null) {
                itens.remove(id, entrada);
                log.info("Cache TTL expirado - id={}", id);
            }
            misses++;
            ultimoHit.set(false);
            return null;
        }

        entrada.ultimoAcesso = System.nanoTime();
        hits++;
        ultimoHit.set(true);
        return entrada.conteudo;
    }

    public synchronized void armazenar(Conteudo conteudo) {
        long agora = System.currentTimeMillis();
        for (Map.Entry<Long, EntradaCache> item : itens.entrySet()) {
            if (agora - item.getValue().armazenadoEm >= ttlMillis) {
                itens.remove(item.getKey(), item.getValue());
            }
        }

        if (!itens.containsKey(conteudo.getId()) && itens.size() >= limite) {
            Map.Entry<Long, EntradaCache> menosUsada = itens.entrySet().stream()
                    .min(Comparator.comparingLong(item -> item.getValue().ultimoAcesso))
                    .orElse(null);

            if (menosUsada != null) {
                itens.remove(menosUsada.getKey(), menosUsada.getValue());
                evictions++;
                log.info("Cache EVICTION - id={} - motivo=LRU", menosUsada.getKey());
            }
        }

        itens.put(conteudo.getId(), new EntradaCache(conteudo, agora));
    }

    public synchronized void invalidar(Long id) {
        itens.remove(id);
    }

    public synchronized Map<String, Object> obterEstatisticas() {
        double hitRatio = hits + misses == 0 ? 0 : hits * 100.0 / (hits + misses);
        return Map.of(
                "tamanhoAtual", itens.size(),
                "limite", limite,
                "hits", hits,
                "misses", misses,
                "evictions", evictions,
                "hitRatio", hitRatio);
    }

    public synchronized void limpar() {
        itens.clear();
        ultimoHit.remove();
    }

    public void registrarBusca(Long id, boolean hit, long tempoMs) {
        String resultado = hit ? "HIT " : "MISS";
        log.info("Cache {} - id={} - {} ms", resultado, id, tempoMs);
    }

    public String consumirResultadoDaBusca() {
        boolean hit = Boolean.TRUE.equals(ultimoHit.get());
        ultimoHit.remove();
        return hit ? "HIT" : "MISS";
    }

    public void limparResultadoDaBusca() {
        ultimoHit.remove();
    }

    private static class EntradaCache {
        private final Conteudo conteudo;
        private final long armazenadoEm;
        private long ultimoAcesso;

        private EntradaCache(Conteudo conteudo, long armazenadoEm) {
            this.conteudo = conteudo;
            this.armazenadoEm = armazenadoEm;
            this.ultimoAcesso = System.nanoTime();
        }
    }
}
