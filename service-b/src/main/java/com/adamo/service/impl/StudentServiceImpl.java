package com.adamo.service.impl;


import com.adamo.dto.Student;
import com.adamo.service.StudentService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StudentServiceImpl implements StudentService {
    Map<String, Student> studentDataBase = new HashMap<>();

    @Override
    public boolean addStudent(Student student) {
        studentDataBase.put(student.getName(), student);
        return true;
    }

    @Override
    public boolean deleteStudent(String name) {
        studentDataBase.remove(name);
        return false;
    }

    @Override
    public List<Student> findAllStudent() {
        Collection<Student> students = studentDataBase.values();
        return new ArrayList<>(students);
    }

    @Override
    public Student findStudentByName(String name) {
        return studentDataBase.get(name);
    }
}
