package com.pshs.ams.controllers;

import java.time.LocalDate;
import java.util.List;

import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import com.pshs.ams.models.dto.attendance.AttendanceDTO;
import com.pshs.ams.models.dto.classroom.ClassroomRankingDTO;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.PageRequest;
import com.pshs.ams.models.dto.custom.SortRequest;
import com.pshs.ams.models.entities.Attendance;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.models.enums.TimeStack;
import com.pshs.ams.models.interfaces.AttendanceForeignEntity;
import com.pshs.ams.services.UtilService;
import com.pshs.ams.services.interfaces.AttendanceService;

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
public class AttendanceController {

	@Inject
	AttendanceService attendanceService;
	private final ModelMapper mapper = new ModelMapper();
	@Inject
	Logger logger;

	@GET
	@Path("/all")
	public Response getAllAttendances(@BeanParam PageRequest pageRequest, @BeanParam SortRequest sortRequest) {
		return Response.ok(
				Attendance.findAll(sortRequest.toSort()).page(pageRequest.toPage()).stream().map(
						attendance -> mapper.map(attendance, AttendanceDTO.class)))
				.build();
	}

	@POST
	@Path("/create")
	public Response createAttendance(AttendanceDTO attendanceDTO, @QueryParam("override") Boolean override) {
		if (attendanceDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Input is null", CodeStatus.NULL))
					.build();
		}
		Attendance attendance = mapper.map(attendanceDTO, Attendance.class);
		CodeStatus status = attendanceService.createAttendance(attendance, override);
		return switch (status) {
			case OK -> {
				if (override) {
					yield Response.status(Response.Status.CREATED)
							.entity(new MessageDTO("Attendance overridden successfully", status))
							.build();
				} else {
					yield Response.status(Response.Status.CREATED)
							.entity(new MessageDTO("Attendance created successfully", status))
							.build();
				}
			}
			case EXISTS -> Response.status(Response.Status.CONFLICT)
					.entity(new MessageDTO("Attendance already exists", status))
					.build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new MessageDTO("Internal Server Error", status))
					.build();
		};
	}

	@PUT
	@Path("/{id}")
	public Response updateAttendance(@PathParam("id") Long id, AttendanceDTO attendanceDTO) {
		try {
			Attendance updatedAttendance = attendanceService.updateAttendance(id, attendanceDTO);
			return Response.ok(mapper.map(updatedAttendance, AttendanceDTO.class))
					.build();
		} catch (NotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity(new MessageDTO(e.getMessage(), CodeStatus.NOT_FOUND))
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new MessageDTO("Error updating attendance", CodeStatus.FAILED))
					.build();
		}
	}

	@GET
	@Path("/count/status")
	public Response countTotalAttendanceByStatus(@QueryParam("attendanceStatuses") String attendanceStatus,
			@BeanParam DateRange dateRange) {
		long count;
		try {
			count = attendanceService.countTotalByAttendanceByStatus(
					UtilService.statusStringToList(attendanceStatus),
					dateRange);
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(e.getMessage(), CodeStatus.BAD_REQUEST))
					.build();
		}

		return Response.ok(count).build();
	}

	@GET
	@Path("/count/last-hour")
	public Response getLastHourAttendance(@QueryParam("attendanceStatuses") String attendanceStatuses) {
		return Response.ok(attendanceService.countLastHourAttendance(UtilService.statusStringToList(attendanceStatuses))).build();
	}

	@GET
	@Path("/chart/pie/classroom/{classroomId}/demographics")
	public Response getClassroomDemographicsChart(@QueryParam("attendanceStatuses") String statuses,
			@BeanParam DateRange dateRange, @PathParam("classroomId") Long id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageDTO(
							"Classroom ID is invalid.",
							CodeStatus.BAD_REQUEST))
					.build();
		}

		try {
			return Response.ok(
					attendanceService.getClassroomAttendanceDemographicsChart(
							UtilService.statusStringToList(statuses),
							dateRange,
							id))
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(e.getMessage(), CodeStatus.BAD_REQUEST))
					.build();
		}
	}

	@GET
	@Path("/chart/line")
	public Response getLineChart(@QueryParam("attendanceStatuses") String statuses, @BeanParam DateRange dateRange,
			@QueryParam("stack") TimeStack stack, @QueryParam("entity") AttendanceForeignEntity foreignEntity,
			@QueryParam("id") Long id) {
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Date range is null or empty", CodeStatus.BAD_REQUEST)).build();
		}

		if (statuses == null || statuses.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Attendance statuses is null or empty", CodeStatus.BAD_REQUEST)).build();
		}

		if (stack == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Time stack is null", CodeStatus.BAD_REQUEST)).build();
		}

		try {
			// LineChartDTO lineChart =
			// attendanceService.getLineChart(UtilService.statusStringToList(statuses),
			// dateRange, stack);
			LineChartDTO lineChart;
			if (foreignEntity != null && id != null) {
				logger.debug("Called getLineChart");
				lineChart = attendanceService.getLineChart(UtilService.statusStringToList(statuses), dateRange, foreignEntity,
						id, stack);
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
	public Response countStudentTotalAttendance(@QueryParam("attendanceStatuses") String statuses,
			@BeanParam DateRange dateRange, @PathParam("id") Long id,
			@PathParam("foreignEntity") AttendanceForeignEntity foreignEntity) throws Exception {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageDTO(
							"Student ID is invalid.",
							CodeStatus.BAD_REQUEST))
					.build();
		}

		if (foreignEntity == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageDTO(
							"Foreign entity is null.",
							CodeStatus.BAD_REQUEST))
					.build();
		}

		if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			return Response.ok(
					attendanceService.countTotalByAttendanceByStatus(UtilService.statusStringToList(statuses), dateRange, id,
							AttendanceForeignEntity.CLASSROOM))
					.build();
		} else {
			return Response.ok(
					attendanceService.countTotalByAttendanceByStatus(UtilService.statusStringToList(statuses), dateRange, id,
							AttendanceForeignEntity.STUDENT))
					.build();
		}
	}

	@GET
	@Path("/{foreignEntity}/{id}/all")
	public Response getForeignEntityAttendances(@QueryParam("attendanceStatuses") String statuses,
			@BeanParam DateRange dateRange, @PathParam("id") Integer id,
			@PathParam("foreignEntity") AttendanceForeignEntity foreignEntity, @BeanParam PageRequest pageRequest,
			@BeanParam SortRequest sortRequest) throws Exception {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageDTO(
							"Student ID is invalid.",
							CodeStatus.BAD_REQUEST))
					.build();
		}

		if (foreignEntity == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageDTO(
							"Foreign entity is null.",
							CodeStatus.BAD_REQUEST))
					.build();
		}

		return Response.ok(
				attendanceService
						.getAllAttendanceByStatusAndDateRange(UtilService.statusStringToList(statuses), dateRange, foreignEntity,
								id, pageRequest.toPage(), sortRequest.toSort())
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
			@BeanParam SortRequest sortRequest) {
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			dateRange = new DateRange(LocalDate.now(), LocalDate.now());
		}
		List<Attendance> attendances = attendanceService.getFilteredAttendances(dateRange, classroomId, gradeLevelId,
				strandId, studentId, pageRequest.toPage(), sortRequest.toSort());
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
			@BeanParam DateRange dateRange) {
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
			@QueryParam("limit") @DefaultValue("5") Integer limit) {
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			dateRange = new DateRange(LocalDate.now(), LocalDate.now());
		}
		List<ClassroomRankingDTO> rankings = attendanceService.getClassroomRanking(dateRange, limit);
		return Response.ok(rankings).build();
	}
}
