package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.attendance.AttendanceDTO;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.entities.Attendance;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.models.interfaces.AttendanceForeignEntity;
import com.pshs.ams.services.UtilService;
import com.pshs.ams.services.interfaces.AttendanceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

@ApplicationScoped
@Path("/api/v1/attendances")
public class AttendanceController {

	@Inject
	AttendanceService attendanceService;
	private final ModelMapper mapper = new ModelMapper();


	@PUT
	@Path("/create")
	public Response createAttendance(AttendanceDTO attendanceDTO) {
		if (attendanceDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity(new MessageDTO("Input is null", CodeStatus.NULL))
				.build();
		}
		Attendance attendance = mapper.map(attendanceDTO, Attendance.class);
		CodeStatus status = attendanceService.createAttendance(attendance);
		return switch (status) {
			case OK -> Response.status(Response.Status.CREATED)
				.entity(new MessageDTO("Attendance created successfully", status))
				.build();
			case EXISTS -> Response.status(Response.Status.CONFLICT)
				.entity(new MessageDTO("Attendance already exists", status))
				.build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(new MessageDTO("Internal Server Error", status))
				.build();
		};
	}


	@GET
	@Path("/count/status")
	public Response countTotalByAttendanceByStatus(@QueryParam("attendanceStatus") String attendanceStatus, @BeanParam DateRange dateRange) {
		long count = 0;
		try {
			count = attendanceService.countTotalByAttendanceByStatus(
				UtilService.statusStringToList(attendanceStatus),
				dateRange
			);
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(e.getMessage(), CodeStatus.BAD_REQUEST)).build();
		}

		return Response.ok(count).build();
	}

	@POST
	@Path("/count/status/entity")
	public Response countTotalByAttendanceByStatus(@QueryParam("attendanceStatus") String attendanceStatus, @BeanParam DateRange dateRange, @QueryParam("entity") AttendanceForeignEntity foreignEntity, @QueryParam("id") Long id) {
		// TODO: Add checks. If the foreignEntity has

		long count;
		try {
			count = attendanceService.countTotalByAttendanceByStatus(
				UtilService.statusStringToList(attendanceStatus),
				dateRange,
				Math.toIntExact(id),
				foreignEntity
			);
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(e.getMessage(), CodeStatus.BAD_REQUEST)).build();
		}

		return Response.ok(count).build();
	}
}