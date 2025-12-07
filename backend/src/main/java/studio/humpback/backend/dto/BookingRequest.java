package studio.humpback.backend.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {

    @NotBlank(message = "{booking.request.name.required}")
    private String name;

    @NotBlank(message = "{booking.request.email.required}")
    @Email(message = "{booking.request.email.invalid}")
    private String email;

    @NotBlank(message = "{booking.request.phone.required}")
    private String phone;

    @NotNull(message = "{booking.request.bookingAt.required}")
    @Future(message = "{booking.request.bookingAt.future}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant bookingAt;

    @NotNull(message = "{booking.request.numberOfHours.required}")
    @Size(min = 2, max = 10, message = "{booking.request.numberOfHours.size}")
    private Integer numberOfHours;

    @NotBlank(message = "{booking.request.type.required}")
    private String type;

}
