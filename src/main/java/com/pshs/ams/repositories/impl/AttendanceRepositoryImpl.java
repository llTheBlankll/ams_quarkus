package com.pshs.ams.repositories.impl;

import com.pshs.ams.models.entities.Attendance;
import com.pshs.ams.repositories.AttendanceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AttendanceRepositoryImpl implements AttendanceRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<Attendance> getFilteredAttendances(LocalDate date, Integer classroomId, Integer gradeLevelId, Integer strandId, Long studentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Attendance> cq = cb.createQuery(Attendance.class);
        Root<Attendance> attendance = cq.from(Attendance.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(attendance.get("date"), date));

        if (classroomId != null) {
            predicates.add(cb.equal(attendance.get("student").get("classroom").get("id"), classroomId));
        }

        if (gradeLevelId != null) {
            predicates.add(cb.equal(attendance.get("student").get("gradeLevel").get("id"), gradeLevelId));
        }

        if (strandId != null) {
            predicates.add(cb.equal(attendance.get("student").get("strand").get("id"), strandId));
        }

        if (studentId != null) {
            predicates.add(cb.equal(attendance.get("student").get("id"), studentId));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getResultList();
    }

    // Implement other attendance-related repository methods here
}
