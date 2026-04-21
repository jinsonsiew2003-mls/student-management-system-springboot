package net.javaguides.sms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.javaguides.sms.entity.Student;
import net.javaguides.sms.service.StudentService;

@Controller
public class StudentController {

	private StudentService studentService;

	private Map<Long, Map<String, Object>> courseStorage = new HashMap<>();
	private Long courseIdCounter = 1L;

	public StudentController(StudentService studentService) {
		super();
		this.studentService = studentService;
	}

	// handler method to handle list students and return model and view
	@GetMapping("/students")
	public String listStudents(Model model) {
		model.addAttribute("students", studentService.getAllStudents());
		return "students";
	}

	@GetMapping("/students/new")
	public String createStudentForm(Model model) {
		Student student = new Student();
		model.addAttribute("student", student);
		return "create_student";
	}

	@PostMapping("/students")
	public String saveStudent(@ModelAttribute("student") Student student) {
		studentService.saveStudent(student);
		return "redirect:/students";
	}

	@GetMapping("/students/edit/{id}")
	public String editStudentForm(@PathVariable Long id, Model model) {
		model.addAttribute("student", studentService.getStudentById(id));
		return "edit_student";
	}

	@PostMapping("/students/{id}")
	public String updateStudent(@PathVariable Long id,
								@ModelAttribute("student") Student student,
								Model model) {

		// get student from database by id
		Student existingStudent = studentService.getStudentById(id);
		existingStudent.setId(id);
		existingStudent.setFirstName(student.getFirstName());
		existingStudent.setLastName(student.getLastName());
		existingStudent.setEmail(student.getEmail());
		existingStudent.setGender(student.getGender());
		existingStudent.setGrade(student.getGrade());

		// save updated student object
		studentService.updateStudent(existingStudent);
		return "redirect:/students";
	}

	// handler method to handle delete student request
	@GetMapping("/students/{id}")
	public String deleteStudent(@PathVariable Long id) {
		studentService.deleteStudentById(id);
		return "redirect:/students";
	}

	@GetMapping("/students/search")
	public String searchStudents(@RequestParam("keyword") String keyword, Model model) {
		List<Student> students = studentService.getAllStudents();

		List<Student> filtered = students.stream()
				.filter(s -> s.getFirstName().toLowerCase().contains(keyword.toLowerCase())
						|| s.getLastName().toLowerCase().contains(keyword.toLowerCase()))
				.toList();

		model.addAttribute("students", filtered);
		return "students";
	}

	// get a list of all courses
	@GetMapping("/api/courses")
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getAllCourses() {
		List<Map<String, Object>> courses = new ArrayList<>(courseStorage.values());
		return ResponseEntity.ok(courses);
	}

	// get a single course based on ID
	@GetMapping("/api/courses/{id}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable Long id) {
		if (courseStorage.containsKey(id)) {
			return ResponseEntity.ok(courseStorage.get(id));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	// create a new course
	@PostMapping("/api/courses")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createCourse(@RequestBody Map<String, Object> course) {
		Long newId = courseIdCounter++;
		course.put("id", newId);
		courseStorage.put(newId, course);
		return ResponseEntity.status(HttpStatus.CREATED).body(course);
	}

	// update course
	@PutMapping("/api/courses/{id}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> updateCourse(@PathVariable Long id,
															@RequestBody Map<String, Object> course) {
		if (courseStorage.containsKey(id)) {
			course.put("id", id);
			courseStorage.put(id, course);
			return ResponseEntity.ok(course);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	// delete course
	@DeleteMapping("/api/courses/{id}")
	@ResponseBody
	public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
		if (courseStorage.containsKey(id)) {
			courseStorage.remove(id);
			return ResponseEntity.ok("Course deleted successfully");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
	}
}