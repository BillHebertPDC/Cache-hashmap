package hashmap.api.mapper;

import hashmap.api.dto.ConteudoDTO;
import hashmap.api.model.Conteudo;

public final class ConteudoMapper {

    private ConteudoMapper() {
    }

    public static Conteudo paraEntidade(ConteudoDTO dto) {
        Conteudo conteudo = new Conteudo();
        conteudo.setTitulo(dto.titulo());
        conteudo.setCorpo(dto.corpo());
        conteudo.setAutor(dto.autor());
        return conteudo;
    }

    public static ConteudoDTO paraResposta(Conteudo conteudo) {
        return new ConteudoDTO(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getCorpo(),
                conteudo.getAutor(),
                conteudo.getAtualizadoEm());
    }
}
