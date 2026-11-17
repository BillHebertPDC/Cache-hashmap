package hashmap.api.cache;

import hashmap.api.model.Conteudo;
import hashmap.api.util.MeuMapa;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConteudoCache {

    private static final Logger log = LoggerFactory.getLogger(ConteudoCache.class);

    private final MeuMapa<Long, EntradaCache> itens = new MeuMapa<>();
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
                itens.remove(id);
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
        for (MeuMapa.Entrada<Long, EntradaCache> item : itens.entradas()) {
            if (agora - item.valor().armazenadoEm >= ttlMillis) {
                itens.remove(item.chave());
            }
        }

        if (!itens.containsKey(conteudo.getId()) && itens.size() >= limite) {
            MeuMapa.Entrada<Long, EntradaCache> menosUsada = null;
            for (MeuMapa.Entrada<Long, EntradaCache> item : itens.entradas()) {
                if (menosUsada == null
                        || item.valor().ultimoAcesso < menosUsada.valor().ultimoAcesso) {
                    menosUsada = item;
                }
            }

            if (menosUsada != null) {
                itens.remove(menosUsada.chave());
                evictions++;
                log.info("Cache EVICTION - id={} - motivo=LRU", menosUsada.chave());
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
