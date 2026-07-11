package com.studenthub.service;

import java.io.ByteArrayInputStream;

public interface ReportService {
    ByteArrayInputStream exportStudentsCsv();
    ByteArrayInputStream exportFacultyCsv();
    ByteArrayInputStream exportCoursesCsv();
}
