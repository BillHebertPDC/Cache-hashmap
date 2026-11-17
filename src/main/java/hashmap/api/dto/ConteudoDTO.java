package hashmap.api.dto;

import java.time.LocalDateTime;

public record ConteudoDTO(
        Long id,
        String titulo,
        String corpo,
        String autor,
        LocalDateTime atualizadoEm) {
}
