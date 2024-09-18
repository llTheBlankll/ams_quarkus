package com.pshs.ams.services;

import com.pshs.ams.models.entities.Student;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StudentService {

	public List<Student> getAllStudents(Sort sort, Page page) {
		return Student.findAll(sort).page(page).list();
	}
}