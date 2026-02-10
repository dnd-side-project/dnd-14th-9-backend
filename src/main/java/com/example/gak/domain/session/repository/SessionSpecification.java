package com.example.gak.domain.session.repository;

import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.enums.DurationRange;
import com.example.gak.domain.session.enums.TimeSlot;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.gak.domain.session.entity.enums.SessionRoomStatus.IN_PROGRESS;
import static com.example.gak.domain.session.entity.enums.SessionRoomStatus.WAITING;

public class SessionSpecification {

    public static Specification<SessionRoom> sessionRoomFilter(
            String keyword,
            SessionCategory category,
            LocalDate startDate,
            LocalDate endDate,
            List<TimeSlot> timeSlots,
            DurationRange durationRange,
            Integer minParticipants,
            Integer requiredFocusRate,
            Integer requiredAchievementRate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    cb.or(
                            cb.equal(root.get("status"), WAITING),
                            cb.equal(root.get("status"), IN_PROGRESS)
                    )
            );

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("title"), "%" + keyword + "%"));
            }

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), endDate.atTime(23,59,59)));
            }

            if (timeSlots != null && !timeSlots.isEmpty()) {
                List<Predicate> slotPredicates = new ArrayList<>();
                for (TimeSlot slot : timeSlots) {
                    switch (slot) {
                        case MORNING -> slotPredicates.add(
                                cb.between(cb.function("HOUR", Integer.class, root.get("startTime")), 6, 11)
                        );
                        case AFTERNOON -> slotPredicates.add(
                                cb.between(cb.function("HOUR", Integer.class, root.get("startTime")), 12, 17)
                        );
                        case EVENING -> slotPredicates.add(
                                cb.between(cb.function("HOUR", Integer.class, root.get("startTime")), 18, 23)
                        );
                    }
                }
                predicates.add(cb.or(slotPredicates.toArray(new Predicate[0])));
            }

            if (durationRange != null) {
                switch (durationRange) {
                    case HALF_TO_ONE_HOUR -> predicates.add(cb.between(root.get("durationMinutes"), 30, 60));
                    case TWO_TO_FOUR_HOURS -> predicates.add(cb.between(root.get("durationMinutes"), 120, 240));
                    case FIVE_TO_EIGHT_HOURS -> predicates.add(cb.between(root.get("durationMinutes"), 300, 480));
                    case TEN_PLUS_HOURS -> predicates.add(cb.greaterThanOrEqualTo(root.get("durationMinutes"), 600));
                }
            }

            if (minParticipants != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxCapacity"), minParticipants));
            }

            if (requiredFocusRate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("requiredFocusRate"), requiredFocusRate));
            }

            if (requiredAchievementRate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("requiredAchievementRate"), requiredAchievementRate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
