package hashmap.api.service;

import hashmap.api.cache.ConteudoCache;
import hashmap.api.model.Conteudo;
import hashmap.api.repository.ConteudoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConteudoService {

    private final ConteudoRepository repository;
    private final ConteudoCache cache;

    public ConteudoService(ConteudoRepository repository, ConteudoCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    public Conteudo buscarPorId(Long id) {
        long inicio = System.currentTimeMillis();
        Conteudo doCache = cache.buscar(id);

        if (doCache != null) {
            cache.registrarBusca(id, true, System.currentTimeMillis() - inicio);
            return doCache;
        }

        try {
            Thread.sleep(2000);
            Conteudo conteudo = repository.findById(id)
                    .orElseThrow(() -> naoEncontrado(id));
            cache.armazenar(conteudo);
            return conteudo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Busca interrompida", e);
        } finally {
            cache.registrarBusca(id, false, System.currentTimeMillis() - inicio);
        }
    }

    public Conteudo cadastrar(Conteudo conteudo) {
        return repository.save(conteudo);
    }

    public Conteudo atualizar(Long id, Conteudo dados) {
        Conteudo existente = repository.findById(id)
                .orElseThrow(() -> naoEncontrado(id));

        existente.setTitulo(dados.getTitulo());
        existente.setCorpo(dados.getCorpo());
        existente.setAutor(dados.getAutor());

        Conteudo atualizado = repository.save(existente);
        cache.invalidar(id);
        return atualizado;
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw naoEncontrado(id);
        }
        repository.deleteById(id);
        cache.invalidar(id);
    }

    private ResponseStatusException naoEncontrado(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Conteúdo não encontrado: " + id);
    }
}
