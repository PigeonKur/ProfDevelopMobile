package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.repository.LearningRepository

class GetAssignedCoursesUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(): List<Course> = repository.getAssignedCourses()
}
