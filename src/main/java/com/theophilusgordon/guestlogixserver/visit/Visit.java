package com.theophilusgordon.guestlogixserver.visit;

import com.theophilusgordon.guestlogixserver.guest.Guest;
import com.theophilusgordon.guestlogixserver.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "guest_id", referencedColumnName = "id")
    private Guest guest;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User host;
    private LocalDateTime checkInDateTime;
    private LocalDateTime checkOutDateTime;
    private byte[] qrCode;
    private Boolean notificationSent;
    private Boolean isExpected;
}