package com.theophilusgordon.guestlogixserver.checkin;

import com.theophilusgordon.guestlogixserver.exception.BadRequestException;
import com.theophilusgordon.guestlogixserver.exception.NotFoundException;
import com.theophilusgordon.guestlogixserver.guest.Guest;
import com.theophilusgordon.guestlogixserver.guest.GuestRepository;
import com.theophilusgordon.guestlogixserver.guest.GuestResponse;
import com.theophilusgordon.guestlogixserver.user.User;
import com.theophilusgordon.guestlogixserver.user.UserRepository;
import com.theophilusgordon.guestlogixserver.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CheckinService {
    private final CheckinRepository checkInRepository;
    private final GuestRepository guestRepository;
    private final UserRepository hostRepository;


    public CheckinResponse checkIn(CheckinRequest request) {
        var checkIn = new Checkin();
        checkIn.setCheckInDateTime(LocalDateTime.now());
        Guest guest = guestRepository.findById(request.getGuestId()).orElseThrow(() -> new NotFoundException("Guest", request.getGuestId()));
        checkIn.setGuest(guest);
        User host = hostRepository.findById(request.getHostId()).orElseThrow(() -> new NotFoundException("User", request.getHostId()));
        checkIn.setHost(host);
        checkInRepository.save(checkIn);
        return this.buildCheckInResponse(checkIn, guest, host);
    }

    public CheckinResponse checkOut(Integer id) {
        var checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CheckInOut", String.valueOf(id)));
        checkIn.setCheckOutDateTime(LocalDateTime.now());
        checkInRepository.save(checkIn);
        return this.buildCheckInResponse(checkIn, checkIn.getGuest(), checkIn.getHost());
    }

    public Iterable<CheckinResponse> getCheckIns() {
        var checkIns = checkInRepository.findAll();
        return checkIns.stream()
                .map(checkin -> this.buildCheckInResponse(checkin, checkin.getGuest(), checkin.getHost()))
                .toList();
    }

    public CheckinResponse getCheckIn(Integer id) {
        var checkIn = checkInRepository.findById(id).orElseThrow(() -> new NotFoundException("CheckInOut", String.valueOf(id)));
        return this.buildCheckInResponse(checkIn, checkIn.getGuest(), checkIn.getHost());
    }

    public Iterable<CheckinResponse> getCheckInsByGuest(String guestId) {
        if(!guestRepository.existsById(guestId))
            throw new NotFoundException("Guest", guestId);

        Iterable<Checkin> checkIns = checkInRepository.findByGuestId(guestId);
        return StreamSupport.stream(checkIns.spliterator(), false)
                .map(checkin -> this.buildCheckInResponse(checkin, checkin.getGuest(), checkin.getHost()))
                .toList();
    }

    public Iterable<CheckinResponse> getCheckInsByHost(String hostId) {
        if(!hostRepository.existsById(hostId))
            throw new NotFoundException("Host", hostId);

        Iterable<Checkin> checkIns = checkInRepository.findByHostId(hostId);
        return StreamSupport.stream(checkIns.spliterator(), false)
                .map(checkin -> this.buildCheckInResponse(checkin, checkin.getGuest(), checkin.getHost()))
                .toList();
    }

    public Iterable<CheckinResponse> getCheckInsByCheckInDate(String checkInDate) {
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(checkInDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Please use the ISO 8601 date time format: yyyy-MM-dd'T'HH:mm:ss");
        }
        var checkIns = checkInRepository.findByCheckInDateTime(dateTime);
        return StreamSupport.stream(checkIns.spliterator(), false)
                .map(checkin -> this.buildCheckInResponse(checkin, checkin.getGuest(), checkin.getHost()))
                .toList();
    }

    public Iterable<CheckinResponse> getCheckInsByPeriod(String start, String end) {
        Pair<LocalDateTime, LocalDateTime> dates = validateAndParseDates(start, end);
        var checkIns = checkInRepository.findByCheckInDateTimeBetween(dates.getFirst(), dates.getSecond());
        return StreamSupport.stream(checkIns.spliterator(), false)
                .map(checkin -> this.buildCheckInResponse(checkin, checkin.getGuest(), checkin.getHost())).
                toList();
    }

    public Iterable<CheckinResponse> getCheckInsByHostAndPeriod(String hostId, String start, String end) {
        if(!hostRepository.existsById(hostId))
            throw new NotFoundException("Host", hostId);

        Pair<LocalDateTime, LocalDateTime> dates = validateAndParseDates(start, end);
        var checkIns = checkInRepository.findByHostIdAndCheckInDateTimeBetween(hostId, dates.getFirst(), dates.getSecond());
        return StreamSupport.stream(checkIns.spliterator(), false)
                .map(checkin -> this.buildCheckInResponse(checkin, checkin.getGuest(), checkin.getHost())).
                toList();
    }

    private CheckinResponse buildCheckInResponse(Checkin checkIn, Guest guest, User host) {
        if(guest == null || host == null)
            throw new BadRequestException("Guest and Host must be provided");
        return CheckinResponse.builder()
                .id(checkIn.getId())
                .checkInDateTime(checkIn.getCheckInDateTime())
                .checkOutDateTime(checkIn.getCheckOutDateTime())
                .guest(GuestResponse.builder()
                        .id(guest.getId())
                        .firstName(guest.getFirstName())
                        .middleName(guest.getMiddleName())
                        .lastName(guest.getLastName())
                        .phone(guest.getPhone())
                        .email(guest.getEmail())
                        .profilePhotoUrl(guest.getProfilePhotoUrl())
                        .build())
                .host(UserResponse.builder()
                        .id(host.getId())
                        .firstName(host.getFirstName())
                        .middleName(host.getMiddleName())
                        .lastName(host.getLastName())
                        .phone(host.getPhone())
                        .email(host.getEmail())
                        .profilePhotoUrl(host.getProfilePhotoUrl())
                        .build())
                .build();
    }

    private Pair<LocalDateTime, LocalDateTime> validateAndParseDates(String start, String end) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        try {
            startDateTime = LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            endDateTime = LocalDateTime.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Please use the ISO 8601 date time format: yyyy-MM-dd'T'HH:mm:ss");
        }

        if (startDateTime.isAfter(endDateTime)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        return Pair.of(startDateTime, endDateTime);
    }
}
