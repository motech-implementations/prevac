package org.motechproject.prevac.web;


import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.impl.csv.writer.CsvTableWriter;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Screening;
import org.motechproject.prevac.domain.UnscheduledVisit;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.dto.CapacityReportDto;
import org.motechproject.prevac.dto.PrimeVaccinationScheduleDto;
import org.motechproject.prevac.dto.UnscheduledVisitDto;
import org.motechproject.prevac.dto.VisitRescheduleDto;
import org.motechproject.prevac.exception.PrevacExportException;
import org.motechproject.prevac.exception.PrevacLookupException;
import org.motechproject.prevac.helper.DtoLookupHelper;
import org.motechproject.prevac.service.ExportService;
import org.motechproject.prevac.service.ReportService;
import org.motechproject.prevac.template.PdfBasicTemplate;
import org.motechproject.prevac.template.PdfExportTemplate;
import org.motechproject.prevac.template.XlsBasicTemplate;
import org.motechproject.prevac.template.XlsExportTemplate;
import org.motechproject.prevac.util.ExcelTableWriter;
import org.motechproject.prevac.util.PdfTableWriter;
import org.motechproject.prevac.util.QueryParamsBuilder;
import org.motechproject.prevac.web.domain.GridSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.motechproject.prevac.constants.PrevacConstants.APPLICATION_PDF_CONTENT;
import static org.motechproject.prevac.constants.PrevacConstants.TEXT_CSV_CONTENT;

@Controller
public class ExportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportController.class);

    private static final String PDF_EXPORT_FORMAT = "pdf";
    private static final String CSV_EXPORT_FORMAT = "csv";
    private static final String XLS_EXPORT_FORMAT = "xls";

    @Autowired
    private ExportService exportService;

    @Autowired
    private ReportService reportService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/exportInstances/screening", method = RequestMethod.GET)
    public void exportScreening(GridSettings settings, @RequestParam String exportRecords,
                                @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForScreeningAndUnscheduled(settings);
        exportEntity(newSettings, exportRecords, outputFormat, response, PrevacConstants.SCREENING_NAME,
                null, Screening.class, PrevacConstants.SCREENING_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/primeVaccinationSchedule", method = RequestMethod.GET)
    public void exportPrimeVaccinationSchedule(GridSettings settings, @RequestParam String exportRecords,
                                               @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForPrimeVaccinationSchedule(settings);

        exportEntity(newSettings, exportRecords, outputFormat, response, PrevacConstants.PRIME_VACCINATION_SCHEDULE_NAME,
                PrimeVaccinationScheduleDto.class, Visit.class, PrevacConstants.PRIME_VACCINATION_SCHEDULE_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/visitReschedule", method = RequestMethod.GET)
    public void exportVisitReschedule(GridSettings settings, @RequestParam String exportRecords,
                                      @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        exportEntity(newSettings, exportRecords, outputFormat, response, PrevacConstants.VISIT_RESCHEDULE_NAME,
                VisitRescheduleDto.class, Visit.class, PrevacConstants.VISIT_RESCHEDULE_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/unscheduledVisits", method = RequestMethod.GET)
    public void exportUnscheduledVisits(GridSettings settings, @RequestParam String exportRecords,
                                        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForScreeningAndUnscheduled(settings);

        exportEntity(newSettings, exportRecords, outputFormat, response, PrevacConstants.UNSCHEDULED_VISITS_NAME,
                UnscheduledVisitDto.class, UnscheduledVisit.class, PrevacConstants.UNSCHEDULED_VISIT_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/capacityReports", method = RequestMethod.GET)
    public void exportCapacityReports(GridSettings settings, @RequestParam String exportRecords,
                                      @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        List<CapacityReportDto> capacityReportDtoList = reportService.generateCapacityReports(settings);

        Integer recordsCount = StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords);

        if (recordsCount != null && capacityReportDtoList.size() > recordsCount) {
            capacityReportDtoList = capacityReportDtoList.subList(0, recordsCount);
        }

        exportEntity(outputFormat, response, PrevacConstants.CAPACITY_REPORT_NAME, capacityReportDtoList, PrevacConstants.CAPACITY_REPORT_FIELDS_MAP);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }

    private void exportEntity(GridSettings settings, String exportRecords, String outputFormat, HttpServletResponse response, //NO CHECKSTYLE ParameterNumber
                              String fileNameBeginning, Class<?> entityDtoType, Class<?> entityType, Map<String, String> headerMap) throws IOException {

        setResponseData(response, outputFormat, fileNameBeginning);

        QueryParams queryParams = new QueryParams(1, StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords),
                QueryParamsBuilder.buildOrderList(settings, getFields(settings)));

        try {
            if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
                PdfBasicTemplate template = new PdfExportTemplate(response.getOutputStream());

                exportService.exportEntityToPDF(template, entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
                exportService.exportEntityToCSV(response.getWriter(), entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
                XlsBasicTemplate template = new XlsExportTemplate(response.getOutputStream());

                exportService.exportEntityToExcel(template, entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            }
        } catch (IOException | PrevacLookupException | PrevacExportException e) {
            LOGGER.debug(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> getFields(GridSettings gridSettings) throws IOException {
        if (gridSettings.getFields() == null) {
            return null;
        } else {
            return objectMapper.readValue(gridSettings.getFields(), new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }

    private void exportEntity(String outputFormat, HttpServletResponse response, String fileNameBeginning, List<?> entities,
                              Map<String, String> headerMap) throws IOException {

        setResponseData(response, outputFormat, fileNameBeginning);

        try {
            if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
                PdfTableWriter tableWriter = new PdfTableWriter(new PdfExportTemplate(response.getOutputStream()));

                exportService.exportEntity(entities, headerMap, tableWriter);
            } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
                CsvTableWriter tableWriter = new CsvTableWriter(response.getWriter());

                exportService.exportEntity(entities, headerMap, tableWriter);
            } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
                ExcelTableWriter tableWriter = new ExcelTableWriter(new XlsExportTemplate(response.getOutputStream()));

                exportService.exportEntity(entities, headerMap, tableWriter);
            }
        } catch (IOException | PrevacLookupException | PrevacExportException e) {
            LOGGER.debug(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void setResponseData(HttpServletResponse response, String outputFormat, String fileNameBeginning) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        final String fileName = fileNameBeginning + "_" + DateTime.now().toString(dateTimeFormatter);

        if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType(APPLICATION_PDF_CONTENT);
        } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType(TEXT_CSV_CONTENT);
        } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType("application/vnd.ms-excel");
        } else {
            throw new IllegalArgumentException("Invalid export format: " + outputFormat);
        }
        response.setCharacterEncoding(UTF_8);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + fileName + "." + outputFormat.toLowerCase());
    }
}
