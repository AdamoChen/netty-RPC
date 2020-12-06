package com.adamo.service;

import org.chen.annotation.RemoteService;
import com.adamo.dto.Student;

import java.util.List;

@RemoteService("service-b")
public interface StudentService {
    boolean addStudent(Student student);

    boolean deleteStudent(String name);

    List<Student> findAllStudent();

    Student findStudentByName(String name);
}
