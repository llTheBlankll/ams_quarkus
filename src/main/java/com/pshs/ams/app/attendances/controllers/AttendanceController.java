package com.pshs.ams.app.attendances.controllers;

import java.time.LocalDate;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;

import com.pshs.ams.app.attendances.models.dto.AttendanceDTO;
import com.pshs.ams.app.classrooms.models.dto.ClassroomRankingDTO;
import com.pshs.ams.global.models.custom.DateRange;
import com.pshs.ams.global.models.custom.LineChart;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import com.pshs.ams.app.attendances.models.entities.Attendance;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.global.models.enums.TimeStack;
import com.pshs.ams.global.models.enums.AttendanceForeignEntity;
import com.pshs.ams.utils.UtilService;
import com.pshs.ams.app.attendances.services.AttendanceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
@Path("/api/v1/attendances")
@Log4j2
public class AttendanceController {

	@Inject
	AttendanceService attendanceService;
	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/all")
	public Response getAllAttendances(@BeanParam PageRequest pageRequest, @BeanParam SortRequest sortRequest) {
		log.debug("AttendanceController - getAllAttendances() - Get all attendances");
		return Response.ok(
				Attendance.findAll(sortRequest.toSort()).page(pageRequest.toPage()).stream().map(
					attendance -> mapper.map(attendance, AttendanceDTO.class)))
			.build();
	}

	@POST
	@Path("/create")
	public Response createAttendance(AttendanceDTO attendanceDTO, @QueryParam("override") Boolean override) {
		if (attendanceDTO == null) {
			log.debug("AttendanceController - createAttendance() - Input is null");
			return Response.status(Response.Status.BAD_REQUEST)
				.entity(new MessageResponse("Input is null", CodeStatus.NULL))
				.build();
		}

		log.debug("AttendanceController - createAttendance() - Creating attendance");
		Attendance attendance = mapper.map(attendanceDTO, Attendance.class);
		log.debug("AttendanceController - createAttendance() - Attendance: {}", attendance);
		CodeStatus status = attendanceService.createAttendance(attendance, override);
		log.debug("AttendanceController - createAttendance() - Status: {}", status);
		return switch (status) {
			case OK -> {
				if (override) {
					log.debug("Attendance overridden successfully");
					yield Response.status(Response.Status.OK)
						.entity(new MessageResponse("Attendance overridden successfully", status))
						.build();
				} else {
					log.debug("Attendance created successfully");
					yield Response.status(Response.Status.OK)
						.entity(new MessageResponse("Attendance created successfully", status))
						.build();
				}
			}
			case EXISTS -> {
				log.debug("Attendance already exists");
				yield Response.status(Response.Status.CONFLICT)
					.entity(new MessageResponse("Attendance already exists", status))
					.build();
			}
			default -> {
				log.debug("Internal Server Error");
				yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new MessageResponse("Internal Server Error", status))
					.build();
			}
		};
	}

	@PUT
	@Path("/{id}")
	public Response updateAttendance(@PathParam("id") Long id, AttendanceDTO attendanceDTO) {
		try {
			Attendance updatedAttendance = attendanceService.updateAttendance(id, attendanceDTO);
			log.debug("AttendanceController - updateAttendance() - Attendance updated successfully");
			return Response.ok(updatedAttendance.toDTO())
				.build();
		} catch (NotFoundException e) {
			log.error("AttendanceController - updateAttendance() - Attendance not found", e);
			return Response.status(Response.Status.NOT_FOUND)
				.entity(new MessageResponse(e.getMessage(), CodeStatus.NOT_FOUND))
				.build();
		} catch (Exception e) {
			log.error("AttendanceController - updateAttendance() - Error updating attendance", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(new MessageResponse("Error updating attendance", CodeStatus.FAILED))
				.build();
		}
	}

	@GET
	@Path("/count/status")
	public Response countTotalAttendanceByStatus(
		@QueryParam("attendanceStatuses") String attendanceStatus,
		@BeanParam DateRange dateRange
	) {
		long count;
		try {
			count = attendanceService.countTotalByAttendanceByStatus(
				UtilService.statusStringToList(attendanceStatus),
				dateRange
			);
		} catch (Exception e) {
			log.error("AttendanceController - countTotalAttendanceByStatus() - Error counting attendances", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(e.getMessage(), CodeStatus.BAD_REQUEST))
				.build();
		}
		log.debug("AttendanceController - countTotalAttendanceByStatus() - count {}", count);
		return Response.ok(count).build();
	}

	@GET
	@Path("/count/last-hour")
	public Response getLastHourAttendance(@QueryParam("attendanceStatuses") String attendanceStatuses) {
		return Response.ok(attendanceService.countLastHourAttendance(UtilService.statusStringToList(attendanceStatuses))).build();
	}

	@GET
	@Path("/chart/pie/classroom/{classroomId}/demographics")
	public Response getClassroomDemographicsChart(
		@QueryParam("attendanceStatuses") String statuses,
		@BeanParam DateRange dateRange, @PathParam("classroomId") Long id
	) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Classroom ID is invalid.",
						CodeStatus.BAD_REQUEST
					))
				.build();
		}

		try {
			return Response.ok(
					attendanceService.getClassroomAttendanceDemographicsChart(
						UtilService.statusStringToList(statuses),
						dateRange,
						id
					))
				.build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(e.getMessage(), CodeStatus.BAD_REQUEST))
				.build();
		}
	}

	@GET
	@Path("/chart/line")
	public Response getLineChart(
		@QueryParam("attendanceStatuses") String statuses, @BeanParam DateRange dateRange,
		@QueryParam("stack") TimeStack stack, @QueryParam("entity") AttendanceForeignEntity foreignEntity,
		@QueryParam("id") Long id
	) {
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity(new MessageResponse("Date range is null or empty", CodeStatus.BAD_REQUEST)).build();
		}

		if (statuses == null || statuses.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity(new MessageResponse("Attendance statuses is null or empty", CodeStatus.BAD_REQUEST)).build();
		}

		if (stack == null) {
			return Response.status(Response.Status.BAD_REQUEST)
				.entity(new MessageResponse("Time stack is null", CodeStatus.BAD_REQUEST)).build();
		}

		try {
			// LineChartDTO lineChart =
			// attendanceService.getLineChart(UtilService.statusStringToList(statuses),
			// dateRange, stack);
			LineChart lineChart;
			if (foreignEntity != null && id != null) {
				log.debug("Called getLineChart");
				lineChart = attendanceService.getLineChart(
					UtilService.statusStringToList(statuses), dateRange, foreignEntity,
					id, stack
				);
			} else {
				lineChart = attendanceService.getLineChart(UtilService.statusStringToList(statuses), dateRange, stack);
			}
			return Response.ok(lineChart).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Path("/{foreignEntity}/{id}/all/count")
	public Response countStudentTotalAttendance(
		@QueryParam("attendanceStatuses") String statuses,
		@BeanParam DateRange dateRange, @PathParam("id") Long id,
		@PathParam("foreignEntity") AttendanceForeignEntity foreignEntity
	) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Student ID is invalid.",
						CodeStatus.BAD_REQUEST
					))
				.build();
		}

		if (foreignEntity == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Foreign entity is null.",
						CodeStatus.BAD_REQUEST
					))
				.build();
		}

		if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			return Response.ok(
					attendanceService.countTotalByAttendanceByStatus(
						UtilService.statusStringToList(statuses), dateRange, id,
						AttendanceForeignEntity.CLASSROOM
					))
				.build();
		} else {
			return Response.ok(
					attendanceService.countTotalByAttendanceByStatus(
						UtilService.statusStringToList(statuses), dateRange, id,
						AttendanceForeignEntity.STUDENT
					))
				.build();
		}
	}

	@GET
	@Path("/{foreignEntity}/{id}/all")
	public Response getForeignEntityAttendances(
		@QueryParam("attendanceStatuses") String statuses,
		@BeanParam DateRange dateRange, @PathParam("id") Integer id,
		@PathParam("foreignEntity") AttendanceForeignEntity foreignEntity, @BeanParam PageRequest pageRequest,
		@BeanParam SortRequest sortRequest
	) throws Exception {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Student ID is invalid.",
						CodeStatus.BAD_REQUEST
					))
				.build();
		}

		if (foreignEntity == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Foreign entity is null.",
						CodeStatus.BAD_REQUEST
					))
				.build();
		}

		return Response.ok(
				attendanceService
					.getAllAttendanceByStatusAndDateRange(
						UtilService.statusStringToList(statuses), dateRange, foreignEntity,
						id, pageRequest.toPage(), sortRequest.toSort()
					)
					.stream().map(attendance -> mapper.map(attendance, AttendanceDTO.class)).toList())
			.build();
	}

	@GET
	@Path("/filtered")
	public Response getFilteredAttendances(
		@QueryParam("classroomId") Integer classroomId,
		@QueryParam("gradeLevelId") Integer gradeLevelId,
		@QueryParam("strandId") Integer strandId,
		@QueryParam("studentId") Long studentId,
		@BeanParam DateRange dateRange,
		@BeanParam PageRequest pageRequest,
		@BeanParam SortRequest sortRequest
	) {
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			dateRange = new DateRange(LocalDate.now(), LocalDate.now());
		}
		List<Attendance> attendances = attendanceService.getFilteredAttendances(
			dateRange, classroomId, gradeLevelId,
			strandId, studentId, pageRequest.toPage(), sortRequest.toSort()
		);
		return Response.ok(
			attendances.stream().map(attendance -> mapper.map(attendance, AttendanceDTO.class)).toList()).build();
	}

	@GET
	@Path("/filtered/count")
	public Response countFilteredAttendances(
		@QueryParam("classroomId") Integer classroomId,
		@QueryParam("gradeLevelId") Integer gradeLevelId,
		@QueryParam("strandId") Integer strandId,
		@QueryParam("studentId") Long studentId,
		@BeanParam DateRange dateRange
	) {
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			dateRange = new DateRange(LocalDate.now(), LocalDate.now());
		}
		return Response
			.ok(attendanceService.countFilteredAttendances(dateRange, classroomId, gradeLevelId, strandId, studentId))
			.build();
	}

	@GET
	@Path("/classroom/ranking")
	public Response getClassroomRanking(
		@BeanParam DateRange dateRange,
		@QueryParam("limit") @DefaultValue("5") Integer limit
	) {
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			dateRange = new DateRange(LocalDate.now(), LocalDate.now());
		}
		List<ClassroomRankingDTO> rankings = attendanceService.getClassroomRanking(dateRange, limit);
		return Response.ok(rankings).build();
	}
}
