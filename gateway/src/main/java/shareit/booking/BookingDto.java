package shareit.booking;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class BookingDto {

    private Long id;

    @NotNull
    private Long itemId;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

}
