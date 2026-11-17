package hashmap.api.controller;

import hashmap.api.cache.ConteudoCache;
import hashmap.api.dto.ConteudoDTO;
import hashmap.api.mapper.ConteudoMapper;
import hashmap.api.model.Conteudo;
import hashmap.api.service.ConteudoService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConteudoController {

    private final ConteudoService service;
    private final ConteudoCache cache;

    public ConteudoController(ConteudoService service, ConteudoCache cache) {
        this.service = service;
        this.cache = cache;
    }

    @PostMapping("/conteudos")
    @ResponseStatus(HttpStatus.CREATED)
    public ConteudoDTO cadastrar(@RequestBody ConteudoDTO dto) {
        Conteudo salvo = service.cadastrar(ConteudoMapper.paraEntidade(dto));
        return ConteudoMapper.paraResposta(salvo);
    }

    @GetMapping("/conteudos/{id}")
    public ResponseEntity<ConteudoDTO> buscar(@PathVariable Long id) {
        try {
            Conteudo conteudo = service.buscarPorId(id);
            String resultadoCache = cache.consumirResultadoDaBusca();
            return ResponseEntity.ok()
                    .header("X-Cache", resultadoCache)
                    .body(ConteudoMapper.paraResposta(conteudo));
        } catch (RuntimeException e) {
            cache.limparResultadoDaBusca();
            throw e;
        }
    }

    @PutMapping("/conteudos/{id}")
    public ConteudoDTO atualizar(
            @PathVariable Long id,
            @RequestBody ConteudoDTO dto) {
        Conteudo atualizado = service.atualizar(id, ConteudoMapper.paraEntidade(dto));
        return ConteudoMapper.paraResposta(atualizado);
    }

    @DeleteMapping("/conteudos/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cache/estatisticas")
    public Map<String, Object> estatisticas() {
        return cache.obterEstatisticas();
    }

    @DeleteMapping("/cache")
    public ResponseEntity<Void> limparCache() {
        cache.limpar();
        return ResponseEntity.noContent().build();
    }
}
