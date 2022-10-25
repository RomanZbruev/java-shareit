package shareit.request;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;


@Builder
@Getter
@Setter
@EqualsAndHashCode
public class RequestDto {

    private Long id;

    private String description;

    private LocalDateTime created;


}
