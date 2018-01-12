(function () {
    'use strict';

    var directives = angular.module('prevac.directives', []);

    /*
* This function checks if the next column is last of the jqgrid.
*/
    function isLastNextColumn(colModel, index) {
        var result;
        $.each(colModel, function (i, val) {
            if ((index + 1) < i) {
                if (colModel[i].hidden !== false) {
                    result = true;
                } else {
                    result = false;
                }
            }
            return (result);
        });
        return result;
    }

    /*
    * This function checks if the field name is reserved for jqgrid (subgrid, cb, rn)
    * and if true temporarily changes that name.
    */
    function changeIfReservedFieldName(fieldName) {
        if (fieldName === 'cb' || fieldName === 'rn' || fieldName === 'subgrid') {
            return fieldName + '___';
        } else {
            return fieldName;
        }
    }

    /*
    * This function checks if the field name was changed
    * and if true changes this name to the original.
    */
    function backToReservedFieldName(fieldName) {
        if (fieldName === 'cb___' || fieldName === 'rn___' || fieldName === 'subgrid___') {
            var fieldNameLength = fieldName.length;
            return fieldName.substring(0, fieldNameLength - 3);
        } else {
            return fieldName;
        }
    }

    /*
    * This function calculates width parameters
    * for fit jqGrid on the screen.
    */
    function resizeGridWidth(gridId) {
        var intervalWidthResize, tableWidth;
        clearInterval(intervalWidthResize);
        intervalWidthResize = setInterval( function () {
            tableWidth = $('.overrideJqgridTable').width();
            $('#' + gridId).jqGrid("setGridWidth", tableWidth);
            clearInterval(intervalWidthResize);
        }, 200);
    }

    /*
    * This function calculates height parameters
    * for fit jqGrid on the screen.
    */
    function resizeGridHeight(gridId) {
        var intervalHeightResize, gap, tableHeight;
        clearInterval(intervalHeightResize);
        intervalHeightResize = setInterval( function () {
            if ($('.overrideJqgridTable').offset() !== undefined) {
                gap = 1 + $('.overrideJqgridTable').offset().top - $('.inner-center .ui-layout-content').offset().top;
                tableHeight = Math.floor($('.inner-center .ui-layout-content').height() - gap - $('.ui-jqgrid-pager').outerHeight() - $('.ui-jqgrid-hdiv').outerHeight());
                $('#' + gridId).jqGrid("setGridHeight", tableHeight);
                resizeGridWidth(gridId);
            }
            clearInterval(intervalHeightResize);
        }, 250);
    }

    /*
    * This function checks grid width
    * and increase this width if possible.
    */
    function resizeIfNarrow(gridId) {
        var intervalIfNarrowResize;
        setTimeout(function() {
            clearInterval(intervalIfNarrowResize);
        }, 950);
        intervalIfNarrowResize = setInterval( function () {
            if (($('#' + gridId).jqGrid('getGridParam', 'width') - 20) > $('#gbox_' + gridId + ' .ui-jqgrid-btable').width()) {
                $('#' + gridId).jqGrid('setGridWidth', ($('#' + gridId).jqGrid('getGridParam', 'width') - 4), true);
                $('#' + gridId).jqGrid('setGridWidth', $('#inner-center.inner-center').width() - 2, false);
            }
        }, 550);
    }

    /*
    * This function checks the name of field
    * whether is selected for display in the jqGrid
    */
    function isSelectedField(name, selectedFields) {
        var i;
        if (selectedFields) {
            for (i = 0; i < selectedFields.length; i += 1) {
                if (name === selectedFields[i].basic.name) {
                    return true;
                }
            }
        }
        return false;
    }

    function handleGridPagination(pgButton, pager, scope) {
        var newPage = 1, last, newSize;
        if ("user" === pgButton) { //Handle changing page by the page input
            newPage = parseInt(pager.find('input:text').val(), 10); // get new page number
            last = parseInt($(this).getGridParam("lastpage"), 10); // get last page number
            if (newPage > last || newPage === 0) { // check range - if we cross range then stop
                return 'stop';
            }
        } else if ("records" === pgButton) { //Page size change, we must update scope value to avoid wrong page size in the trash screen
            newSize = parseInt(pager.find('select')[0].value, 10);
            scope.entityAdvanced.userPreferences.gridRowsNumber = newSize;
        }
    }

    function buildGridColModel(colModel, fields, scope, ignoreHideFields) {
        var i, cmd, field;

        for (i = 0; i < fields.length; i += 1) {
            field = fields[i];

            if (!field.nonDisplayable) {
                //if name is reserved for jqgrid need to change field name
                field.basic.name = changeIfReservedFieldName(field.basic.name);

                cmd = {
                    label: field.basic.displayName,
                    name: field.basic.name,
                    index: field.basic.name,
                    jsonmap: field.basic.name,
                    width: 220,
                    hidden: ignoreHideFields? false : !isSelectedField(field.basic.name, scope.selectedFields)
                };

                colModel.push(cmd);
            }
        }
    }

    function handleUndefined(value) {
        if (value === undefined) {
            value = "";
        }
        return value;
    }

    directives.directive('timePicker', function($timeout) {
        return {
            restrict: 'A',
            require: 'ngModel',
            transclude: true,
            link: function(scope, element, attrs, ngModel) {
                $timeout(function() {
                    var elem = angular.element(element);

                    elem.datetimepicker({
                        dateFormat: "",
                        timeOnly: true,
                        timeFormat: "HH:mm",
                        onSelect: function (selectedTime) {
                            scope.$apply(function() {
                                ngModel.$setViewValue(selectedTime);
                            });
                        }
                    });
                });
            }
        };
    });

    directives.directive('gridReloadTrigger', function() {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function(scope, element, attrs) {
                scope.$watch("$parent." + attrs.ngModel, function () {
                    $(".prevac-grid").jqGrid('setGridParam', {
                        datatype: 'json',
                        page: 1,
                        postData: {
                            fields: JSON.stringify(scope.lookupBy),
                            lookup: (scope.selectedLookup) ? scope.selectedLookup.lookupName : ""
                        }
                    }).trigger('reloadGrid');
                });
            }
        };
    });

    directives.directive('prevacDatePicker', ['$timeout', function($timeout) {

        return {
            restrict: 'A',
            scope: {
                min: '@',
                max: '@'
            },
            require: 'ngModel',
            link: function (scope, element, attr, ngModel) {
                var isReadOnly = scope.$eval(attr.ngReadonly);
                if(!isReadOnly) {
                    angular.element(element).datepicker({
                        changeYear: true,
                        showButtonPanel: true,
                        dateFormat: 'yy-mm-dd',
                        onSelect: function (dateTex) {
                            $timeout(function() {
                                ngModel.$setViewValue(dateTex);
                            })
                        },
                        onChangeMonthYear: function (year, month, inst) {
                            var curDate = $(this).datepicker("getDate");
                            if (curDate === null) {
                                return;
                            }
                            if (curDate.getFullYear() !== year || curDate.getMonth() !== month - 1) {
                                curDate.setYear(year);
                                curDate.setMonth(month - 1);
                                $(this).datepicker("setDate", curDate);
                            }
                        },
                        onClose: function (dateText, inst) {
                            var viewValue = element.val();
                            $timeout(function() {
                                ngModel.$setViewValue(viewValue);
                            })
                        }
                    });
                }

                scope.$watch("$parent." + scope.min, function(value) {
                    if (value !== undefined && value !== '') {
                        angular.element(element).datepicker('option', 'minDate', value);
                    }
                });

                scope.$watch("$parent." + scope.max, function(value) {
                    if (value !== undefined && value !== '') {
                        angular.element(element).datepicker('option', 'maxDate', value);
                    }
                });
            }
        };
    }]);

    directives.directive('screeningGrid', function ($compile, $timeout) {

        function createButton(id) {
            return '<button type="button" class="btn btn-primary btn-sm ng-binding compileBtn" ng-click="printRow(' +
                               id + ')"><i class="fa fa-fw fa-print"></i></button>';
        }

        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element), eventChange, eventResize;

                elem.jqGrid({
                    url: "../prevac/screenings",
                    datatype: "json",
                    mtype: "GET",
                    colNames: [
                        scope.msg("prevac.location"),
                        scope.msg("prevac.screening.bookingId"),
                        scope.msg("prevac.screening.status"),
                        scope.msg("prevac.screening.date"),
                        scope.msg("prevac.screening.time"),
                        "", ""],
                    colModel: [
                        { name: "clinic.location" },
                        { name: "volunteer.id" },
                        { name: "status" },
                        { name: "date" },
                        { name: "startTime" },
                        { name: "print", align: "center", sortable: false, width: 40 },
                        { name: "changeStatus", align: "center", sortable: false, width: 60,
                             formatter: function(cellValue, options, rowObject) {
                                 if (rowObject.status === 'Active') {
                                     return "<button ng-click='cancel(\"" + rowObject.id + "\")'" +
                                             " type='button' class='btn btn-danger compileBtn' ng-disabled='updateInProgress'>" +
                                             scope.msg('prevac.screening.btn.cancel') + "</button>";
                                 } else if (rowObject.status === 'Canceled') {
                                     return "<button ng-click='activate(\"" + rowObject.id + "\")'" +
                                             " type='button' class='btn btn-success compileBtn' ng-disabled='updateInProgress'>" +
                                             scope.msg('prevac.screening.btn.activate') + "</button>";
                                 }
                                 return '';
                             }
                        }
                    ],
                    gridComplete: function() {
                        var ids = elem.getDataIDs();
                        for(var i = 0; i < ids.length; i++){
                            elem.setRowData(ids[i], {print: createButton(ids[i])})
                        }
                        $compile($('.compileBtn'))(scope);
                        $('#screeningTable .ui-jqgrid-hdiv').addClass("table-lightblue");
                        $('#screeningTable .ui-jqgrid-btable').addClass("table-lightblue");
                    },
                    pager: "#pager",
                    rowNum: 50,
                    rowList: [10, 20, 50, 100],
                    prmNames: {
                        sort: 'sortColumn',
                        order: 'sortDirection'
                    },
                    sortname: null,
                    sortorder: "desc",
                    viewrecords: true,
                    gridview: true,
                    loadOnce: false,
                    postData: {
                        startDate: function() {
                            return handleUndefined(scope.selectedFilter.startDate);
                        },
                        endDate: function() {
                            return handleUndefined(scope.selectedFilter.endDate);
                        },
                        dateFilter: function() {
                            return handleUndefined(scope.selectedFilter.dateFilter);
                        }
                    },
                    beforeSelectRow: function() {
                        return false;
                    },
                    onCellSelect: function (id, iCol, cellContent, e) {
                        if (iCol !== 5 && iCol !== 6) {
                            scope.editScreening(id);
                        }
                    }
                });

                scope.$watch("lookupRefresh", function () {
                    $('#' + attrs.id).jqGrid('setGridParam', {
                        page: 1,
                        postData: {
                            fields: JSON.stringify(scope.lookupBy),
                            lookup: (scope.selectedLookup) ? scope.selectedLookup.lookupName : ""
                        }
                    }).trigger('reloadGrid');
                });

                $(window).on('resize', function() {
                    clearTimeout(eventResize);
                    eventResize = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                        $(".ui-layout-content").scrollTop(0);
                    }, 200);
                }).trigger('resize');

                $('#inner-center').on('change', function() {
                    clearTimeout(eventChange);
                    eventChange = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                    }, 200);
                });

            }
        };
    });

    directives.directive('unscheduledVisitGrid', function ($compile, $timeout) {

        var gridDataExtension;

        function extendGrid(cellValue, options, rowObject) {
            var rowExtraData = {};

            rowExtraData.id = rowObject.id;
            rowExtraData.siteId = rowObject.siteId;

            gridDataExtension[options.rowId] = rowExtraData;

            return cellValue;
        }

        function createButton(id) {
            return '<button type="button" class="btn btn-primary btn-sm ng-binding printBtn" ng-click="printFrom(' +
                               id + ')"><i class="fa fa-fw fa-print"></i></button>';
        }

        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element), eventChange, eventResize;

                elem.jqGrid({
                    url: "../prevac/unscheduledVisits",
                    datatype: "json",
                    mtype: "GET",
                    colNames: [
                        scope.msg("prevac.uncheduledVisit.participantId"),
                        scope.msg("prevac.location"),
                        scope.msg("prevac.date"),
                        scope.msg("prevac.startTime"),
                        scope.msg("prevac.uncheduledVisit.purpose"),
                        ""],
                    colModel: [
                        {
                            name: "participantId",
                            formatter: extendGrid,
                            index: 'subject.subjectId'
                        },
                        {
                            name: "clinicName",
                            index: 'subject.siteName'
                        },
                        {
                            name: "date"
                        },
                        {
                            name: "startTime"
                        },
                        {
                            name: "purpose"
                        },
                        {
                            name: "print",
                            align: "center",
                            sortable: false,
                            hidden: true
                        }
                    ],
                    gridComplete: function() {
                        var ids = elem.getDataIDs();
                        for(var i = 0; i < ids.length; i++){
                            elem.setRowData(ids[i], {print: createButton(ids[i])})
                        }
                        $compile($('.printBtn'))(scope);
                        $('#unscheduledVisitTable .ui-jqgrid-hdiv').addClass("table-lightblue");
                        $('#unscheduledVisitTable .ui-jqgrid-btable').addClass("table-lightblue");
                    },
                    pager: "#pager",
                    rowNum: 50,
                    rowList: [10, 20, 50, 100],
                    prmNames: {
                        sort: 'sortColumn',
                        order: 'sortDirection'
                    },
                    sortname: null,
                    sortorder: "desc",
                    viewrecords: true,
                    gridview: true,
                    loadOnce: false,
                    beforeRequest: function() {
                        gridDataExtension = [];
                    },
                    onCellSelect: function(rowId, iCol, cellContent, e) {
                        if (iCol !== 5) {
                            var rowData = elem.getRowData(rowId),
                                extraRowData = gridDataExtension[rowId];

                            scope.newForm();
                            scope.form.dto.id = extraRowData.id;
                            scope.form.dto.participantId = rowData.participantId;
                            scope.form.dto.date = rowData.date;
                            scope.form.dto.startTime = rowData.startTime;
                            scope.form.dto.purpose = rowData.purpose;
                            scope.reloadSelects();
                            $('#unscheduledVisitModal').modal('show');
                        }
                    },
                    postData: {
                        startDate: function() {
                            return handleUndefined(scope.selectedFilter.startDate);
                        },
                        endDate: function() {
                            return handleUndefined(scope.selectedFilter.endDate);
                        },
                        dateFilter: function() {
                            return handleUndefined(scope.selectedFilter.dateFilter);
                        }
                    },
                    beforeSelectRow: function() {
                        return false;
                    }
                });

                scope.$watch("lookupRefresh", function () {
                    $('#' + attrs.id).jqGrid('setGridParam', {
                        page: 1,
                        postData: {
                            fields: JSON.stringify(scope.lookupBy),
                            lookup: (scope.selectedLookup) ? scope.selectedLookup.lookupName : ""
                        }
                    }).trigger('reloadGrid');
                });

                $(window).on('resize', function() {
                    clearTimeout(eventResize);
                    eventResize = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                        $(".ui-layout-content").scrollTop(0);
                    }, 200);
                }).trigger('resize');

                $('#inner-center').on('change', function() {
                    clearTimeout(eventChange);
                    eventChange = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                    }, 200);
                });
            }
        };
    });

    directives.directive('primeVaccinationGrid', function ($compile, $timeout) {

        var gridDataExtension;
        var rowsToColor = [];

        function createButton(rowId) {
            return '<button type="button" class="btn btn-primary btn-sm ng-binding printBtn" ng-click="printCardFrom(' +
            rowId + ')">' + '<i class="fa fa-fw fa-print"></i>' + '</button>'
        }

        function extendGrid(cellValue, options, rowObject) {
            var rowExtraData = {};

            rowExtraData.visitId = rowObject.visitId;
            rowExtraData.siteId = rowObject.siteId;
            rowExtraData.participantGender = rowObject.participantGender;
            rowExtraData.ignoreDateLimitation = rowObject.ignoreDateLimitation;

            gridDataExtension[options.rowId] = rowExtraData;

            return cellValue;
        }

        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element), eventChange, eventResize;

                elem.jqGrid({
                    url: "../prevac/primeVaccinationSchedule",
                    datatype: "json",
                    mtype: "GET",
                    colNames: [
                        scope.msg("prevac.location"),
                        scope.msg("prevac.primeVaccination.participantId"),
                        scope.msg("prevac.primeVaccination.participantName"),
                        scope.msg("prevac.primeVaccination.femaleChildBearingAge"),
                        scope.msg("prevac.primeVaccination.screeningActualDate"),
                        scope.msg("prevac.primeVaccination.primeVacDate"),
                        scope.msg("prevac.primeVaccination.time"),
                        ""],
                    colModel: [
                        {
                            name: "location",
                            index: 'subject.siteName'
                        },
                        {
                            name: "participantId",
                            formatter: extendGrid,
                            index: 'subject.subjectId'
                        },
                        {
                            name: "participantName",
                            formatter: rowColorFormatter,
                            index: 'subject.name'
                        },
                        {
                            name: "femaleChildBearingAge",
                            index: 'subject.femaleChildBearingAge'
                        },
                        {
                            name: "actualScreeningDate",
                            sortable: false
                        },
                        {
                            name: "date",
                            index: 'dateProjected'
                        },
                        {
                            name: "startTime"
                        },
                        {
                            name: "print",
                            align: "center",
                            sortable: false,
                            width: 40
                        }
                    ],
                    gridComplete: function(){
                        var ids = elem.getDataIDs();
                            for(var i=0;i<ids.length;i++){
                                elem.setRowData(ids[i],{print: createButton(ids[i])})
                            }
                        $compile($('.printBtn'))(scope);
                        $('#primeVaccinationTable .ui-jqgrid-hdiv').addClass("table-lightblue");
                        $('#primeVaccinationTable .ui-jqgrid-btable').addClass("table-lightblue");
                        for (var i = 0; i < rowsToColor.length; i++) {
                            $("#" + rowsToColor[i]).find("td").css("color", "red");
                        }
                    },
                    pager: "#pager",
                    rowNum: 50,
                    rowList: [10, 20, 50, 100],
                    prmNames: {
                        sort: 'sortColumn',
                        order: 'sortDirection'
                    },
                    sortname: null,
                    sortorder: "desc",
                    viewrecords: true,
                    gridview: true,
                    loadOnce: false,
                    beforeSelectRow: function() {
                        return false;
                    },
                    beforeRequest: function() {
                        gridDataExtension = [];
                        rowsToColor = [];
                    },
                    onCellSelect: function(rowId, iCol, cellContent, e) {
                        if (iCol !== 7) {
                            var rowData = elem.getRowData(rowId),
                                extraRowData = gridDataExtension[rowId];
                            scope.newForm("edit");
                            scope.form.dto.visitId = extraRowData.visitId;
                            scope.form.dto.participantId = rowData.participantId;
                            scope.form.dto.participantName = rowData.participantName;
                            scope.form.dto.femaleChildBearingAge = rowData.femaleChildBearingAge;
                            scope.form.dto.actualScreeningDate = rowData.actualScreeningDate;
                            scope.form.dto.date = rowData.date;
                            scope.form.dto.startTime = rowData.startTime;
                            scope.form.dto.participantGender = extraRowData.participantGender;
                            scope.form.dto.ignoreDateLimitation = extraRowData.ignoreDateLimitation;
                            scope.form.range = scope.calculateRange(scope.form.dto.actualScreeningDate,
                                scope.form.dto.femaleChildBearingAge, scope.form.dto.ignoreDateLimitation);
                            scope.reloadSelects();
                            $('#primeVaccinationScheduleModal').modal('show');
                        }
                    },
                    postData: {
                        startDate: function() {
                            return handleUndefined(scope.selectedFilter.startDate);
                        },
                        endDate: function() {
                            return handleUndefined(scope.selectedFilter.endDate);
                        },
                        dateFilter: function() {
                            return handleUndefined(scope.selectedFilter.dateFilter);
                        }
                    }
                });

                scope.$watch("lookupRefresh", function () {
                    $('#' + attrs.id).jqGrid('setGridParam', {
                        page: 1,
                        postData: {
                            fields: JSON.stringify(scope.lookupBy),
                            lookup: (scope.selectedLookup) ? scope.selectedLookup.lookupName : ""
                        }
                    }).trigger('reloadGrid');
                });

                function rowColorFormatter(cellValue, options, rowObject) {
                    var range = scope.calculateRangeForGrid(rowObject.actualScreeningDate,
                        rowObject.femaleChildBearingAge, false);
                    range.min.setHours(0,0,0,0);
                    range.max.setHours(23,59,59,0);
                    var min = range.min.getTime();
                    var max = range.max.getTime();
                    var bookingDate = Date.parse(rowObject.date);
                    if ((max < bookingDate || min > bookingDate) && !rowObject.ignoreDateLimitation) {
                        rowsToColor[rowsToColor.length] = options.rowId;
                    }
                    return cellValue;
                }

                $(window).on('resize', function() {
                    clearTimeout(eventResize);
                    eventResize = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                        $(".ui-layout-content").scrollTop(0);
                    }, 200);
                }).trigger('resize');

                $('#inner-center').on('change', function() {
                    clearTimeout(eventChange);
                    eventChange = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                    }, 200);
                });
            }
        };
    });

    directives.directive('visitRescheduleGrid', function ($compile, $timeout) {

        var gridDataExtension;
        var rowsToColor = [];

        function createButton(id) {
            return '<button type="button" class="btn btn-primary btn-sm ng-binding printBtn" ng-click="print()"><i class="fa fa-fw fa-print"></i></button>';
        }

        function extendGrid(cellValue, options, rowObject) {
            var rowExtraData = {};

            rowExtraData.siteId = rowObject.siteId;
            rowExtraData.visitId = rowObject.visitId;
            rowExtraData.earliestDate = rowObject.earliestDate;
            rowExtraData.latestDate = rowObject.latestDate;
            rowExtraData.ignoreDateLimitation = rowObject.ignoreDateLimitation;
            rowExtraData.boosterRelated = rowObject.boosterRelated;
            rowExtraData.notVaccinated = rowObject.notVaccinated;

            gridDataExtension[options.rowId] = rowExtraData;

            return cellValue;
        }

        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element), eventChange, eventResize;

                elem.jqGrid({
                    url: "../prevac/visitReschedule",
                    datatype: "json",
                    mtype: "GET",
                    colNames: [
                        scope.msg("prevac.location"),
                        scope.msg("prevac.visitReschedule.participantId"),
                        scope.msg("prevac.visitReschedule.participantName"),
                        scope.msg("prevac.visitReschedule.visitType"),
                        scope.msg("prevac.visitReschedule.actualDate"),
                        scope.msg("prevac.visitReschedule.plannedDate"),
                        scope.msg("prevac.visitReschedule.time"),
                        ""],
                    colModel: [
                        {
                            name: "location",
                            index: 'subject.siteName'
                        },
                        {
                            name: "participantId",
                            formatter: extendGrid,
                            index: 'subject.subjectId'
                        },
                        {
                            name: "participantName",
                            index: 'subject.name'
                        },
                        {
                            name: "visitType",
                            index: 'type'
                        },
                        {
                            name: "actualDate",
                            index: 'date'
                        },
                        {
                            name: "plannedDate",
                            formatter: rowColorFormatter,
                            index: 'dateProjected'
                        },
                        {
                            name: "startTime"
                        },
                        {
                            name: "print", align: "center", sortable: false, width: 60
                        }
                        ],
                    gridComplete: function(){
                        var ids = elem.getDataIDs();
                        for(var i = 0; i < ids.length; i++){
                            elem.setRowData(ids[i], {print: createButton(ids[i])})
                        }
                        $compile($('.printBtn'))(scope);
                        $('#visitRescheduleTable .ui-jqgrid-hdiv').addClass("table-lightblue");
                        $('#visitRescheduleTable .ui-jqgrid-btable').addClass("table-lightblue");
                        for (var i = 0; i < rowsToColor.length; i++) {
                            $("#" + rowsToColor[i]).find("td").css("color", "red");
                        }
                    },
                    pager: "#pager",
                    rowNum: 50,
                    rowList: [10, 20, 50, 100],
                    prmNames: {
                        sort: 'sortColumn',
                        order: 'sortDirection'
                    },
                    sortname: null,
                    sortorder: "desc",
                    viewrecords: true,
                    gridview: true,
                    loadOnce: false,
                    beforeSelectRow: function() {
                        return false;
                    },
                    beforeRequest: function() {
                        gridDataExtension = [];
                        rowsToColor = [];
                    },
                    onCellSelect: function(rowId, iCol, cellContent, e) {
                        if (iCol !== 8) {
                            var rowData = elem.getRowData(rowId),
                                extraRowData = gridDataExtension[rowId];

                            if (rowData.actualDate !== undefined && rowData.actualDate !== null && rowData.actualDate !== '') {
                                scope.visitForPrint = elem.getRowData(rowId);
                                scope.form = null;
                                scope.showRescheduleModal(scope.msg('prevac.visitReschedule.cannotReschedule'), scope.msg('prevac.visitReschedule.visitWithActualDate'));
                            } else if (extraRowData.earliestDate === undefined || extraRowData.earliestDate === null || extraRowData.earliestDate === "") {
                                scope.visitForPrint = elem.getRowData(rowId);
                                scope.form = null;
                                var message = "prevac.visitReschedule.participantVisitScheduleOffsetMissing";

                                if (extraRowData.notVaccinated) {
                                    if (extraRowData.boosterRelated) {
                                        message = "prevac.visitReschedule.participantNotBoostVaccinated";
                                    } else {
                                        message = "prevac.visitReschedule.participantNotPrimeVaccinated";
                                    }
                                }

                                scope.showRescheduleModal(scope.msg('prevac.visitReschedule.cannotReschedule'), scope.msg(message));
                            } else if (extraRowData.latestDate === undefined || extraRowData.latestDate === null || extraRowData.latestDate === "") {
                                scope.visitForPrint = elem.getRowData(rowId);
                                scope.form = null;
                                scope.showRescheduleModal(scope.msg('prevac.visitReschedule.cannotReschedule'), scope.msg('prevac.visitReschedule.visitNotInRescheduleWindow'));
                            } else {
                                scope.newForm();
                                scope.form.dto.participantId = rowData.participantId;
                                scope.form.dto.participantName = rowData.participantName;
                                scope.form.dto.visitType = rowData.visitType;
                                scope.form.dto.plannedDate = rowData.plannedDate;
                                scope.form.dto.startTime = rowData.startTime;
                                scope.form.dto.visitId = extraRowData.visitId;
                                scope.form.dto.ignoreDateLimitation = extraRowData.ignoreDateLimitation;
                                scope.earliestDateToReturn = scope.parseDate(extraRowData.earliestDate);
                                scope.latestDateToReturn = scope.parseDate(extraRowData.latestDate);
                                if (!scope.form.dto.ignoreDateLimitation) {
                                    scope.form.dto.minDate = scope.earliestDateToReturn;
                                    scope.form.dto.maxDate = scope.latestDateToReturn;
                                } else {
                                    scope.form.dto.minDate = new Date();
                                    scope.form.dto.maxDate = null;
                                }
                                scope.showRescheduleModal(scope.msg('prevac.visitReschedule.update'), scope.msg('prevac.visitReschedule.updateSuccessful'));
                            }
                        } else {
                            scope.visitForPrint = elem.getRowData(rowId);
                        }
                    },
                    postData: {
                        startDate: function() {
                            return handleUndefined(scope.selectedFilter.startDate);
                        },
                        endDate: function() {
                            return handleUndefined(scope.selectedFilter.endDate);
                        },
                        dateFilter: function() {
                            return handleUndefined(scope.selectedFilter.dateFilter);
                        }
                    }
                });

                scope.$watch("lookupRefresh", function () {
                    $('#' + attrs.id).jqGrid('setGridParam', {
                        page: 1,
                        postData: {
                            fields: JSON.stringify(scope.lookupBy),
                            lookup: (scope.selectedLookup) ? scope.selectedLookup.lookupName : ""
                        }
                    }).trigger('reloadGrid');
                });

                function rowColorFormatter(cellValue, options, rowObject) {
                    var min = Date.parse(rowObject.earliestWindowDate);
                    var max = Date.parse(rowObject.latestDate);
                    var bookingDate = Date.parse(rowObject.plannedDate);
                    if (max !== null && max !== undefined && min !== null && min !== undefined &&
                          (max < bookingDate || min > bookingDate) && !rowObject.ignoreDateLimitation &&
                          (rowObject.actualDate === null || rowObject.actualDate === undefined || rowObject.actualDate === "")) {
                       rowsToColor[rowsToColor.length] = options.rowId;
                    }
                    return cellValue;
                }

                $(window).on('resize', function() {
                    clearTimeout(eventResize);
                    eventResize = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                        $(".ui-layout-content").scrollTop(0);
                    }, 200);
                }).trigger('resize');

                $('#inner-center').on('change', function() {
                    clearTimeout(eventChange);
                    eventChange = $timeout(function() {
                        scope.resizeGridWidth(attrs.id);
                        scope.resizeGridHeight(attrs.id);
                    }, 200);
                });
            }
        };
    });

    directives.directive('capacityInfoGrid', function ($timeout) {

        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element), eventChange, eventResize;

                elem.jqGrid({
                    url: "../prevac/capacity/getCapacityInfo",
                    datatype: "json",
                    mtype: "GET",
                    colNames: [
                        scope.msg("prevac.capacityInfo.clinic"),
                        scope.msg("prevac.capacityInfo.maxCapacity"),
                        scope.msg("prevac.capacityInfo.availableCapacity"),
                        scope.msg("prevac.capacityInfo.screeningSlotRemaining"),
                        scope.msg("prevac.capacityInfo.vaccineSlotRemaining")],
                    colModel: [
                        {
                            name: "clinic",
                            index: 'location'
                        },
                        {
                            name: "maxCapacity",
                            index: 'maxCapacityByDay'
                        },
                        {
                            name: "availableCapacity",
                            sortable: false
                        },
                        {
                            name: "screeningSlotRemaining",
                            sortable: false
                        },
                        {
                            name: "vaccineSlotRemaining",
                            sortable: false
                        }
                    ],
                    gridComplete: function(){
                        $('#capacityInfoTable .ui-jqgrid-hdiv').addClass("table-lightblue");
                        $('#capacityInfoTable .ui-jqgrid-btable').addClass("table-lightblue");
                    },
                    pager: "#pager",
                    rowNum: 50,
                    rowList: [10, 20, 50, 100],
                    prmNames: {
                        sort: 'sortColumn',
                        order: 'sortDirection'
                    },
                    sortname: null,
                    sortorder: "desc",
                    viewrecords: true,
                    gridview: true,
                    loadOnce: false,
                    beforeSelectRow: function() {
                        return false;
                    },
                    postData: {
                        startDate: function() {
                            return handleUndefined(scope.selectedFilter.startDate);
                        },
                        endDate: function() {
                            return handleUndefined(scope.selectedFilter.endDate);
                        },
                        dateFilter: function() {
                            return handleUndefined(scope.selectedFilter.dateFilter);
                        }
                    }
                });

                scope.$watch("lookupRefresh", function () {
                    $('#' + attrs.id).jqGrid('setGridParam', {
                        page: 1
                    }).trigger('reloadGrid');
                });

                $(window).on('resize', function() {
                    clearTimeout(eventResize);
                    eventResize = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                        $(".ui-layout-content").scrollTop(0);
                    }, 200);
                }).trigger('resize');

                $('#inner-center').on('change', function() {
                    clearTimeout(eventChange);
                    eventChange = $timeout(function() {
                        scope.resizeGridHeight(attrs.id);
                        scope.resizeGridWidth(attrs.id);
                    }, 200);
                });
            }
        };
    });

    directives.directive('capacityReportGrid', function () {

        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element);

                elem.jqGrid({
                    url: '../prevac/getCapacityReports',
                    datatype: 'json',
                    mtype: 'GET',
                    prmNames: {
                        sort: 'sortColumn',
                        order: 'sortDirection'
                    },
                    rowNum: 50,
                    rowList: [10, 20, 50, 100],
                    colNames: [
                        scope.msg("prevac.capacityReport.date"),
                        scope.msg("prevac.capacityReport.clinic"),
                        scope.msg("prevac.capacityInfo.maxCapacity"),
                        scope.msg("prevac.capacityInfo.availableCapacity"),
                        scope.msg("prevac.capacityInfo.screeningSlotRemaining"),
                        scope.msg("prevac.capacityInfo.vaccineSlotRemaining")],
                    colModel: [
                        {
                            name: 'date',
                            index: 'date'
                        },
                        {
                            name: 'location',
                            index: 'location'
                        },
                        {
                            name: 'maxCapacity',
                            sorttype: 'int'
                        },
                        {
                            name: 'availableCapacity',
                            sorttype: 'int'
                        },
                        {
                            name: 'screeningSlotRemaining',
                            sorttype: 'int'
                        },
                        {
                            name: 'vaccineSlotRemaining',
                            sorttype: 'int'
                        }
                    ],
                    pager: '#pager',
                    sortname: null,
                    sortorder: 'asc',
                    viewrecords: true,
                    loadonce: true,
                    gridview: true,
                    gridComplete: function () {
                        $('#capacityReportTable .ui-jqgrid-hdiv').addClass("table-lightblue");
                        $('#capacityReportTable .ui-jqgrid-btable').addClass("table-lightblue");
                    },
                    postData: {
                        startDate: function() {
                            return handleUndefined(scope.selectedFilter.startDate);
                        },
                        endDate: function() {
                            return handleUndefined(scope.selectedFilter.endDate);
                        },
                        dateFilter: function() {
                            return handleUndefined(scope.selectedFilter.dateFilter);
                        }
                    }
                });

                scope.$watch("lookupRefresh", function () {
                    $('#' + attrs.id).jqGrid('setGridParam', {
                        datatype: 'json',
                        page: 1,
                        postData: {
                            fields: JSON.stringify(scope.lookupBy),
                            lookup: (scope.selectedLookup) ? scope.selectedLookup.lookupName : ""
                        }
                    }).trigger('reloadGrid');
                });
            }
        };
    });

    directives.directive('prevacInstancesGrid', function ($rootScope, $route, $timeout) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var elem = angular.element(element), tableWidth, eventResize, eventChange,
                    gridId = attrs.id,
                    firstLoad = true;

                $.ajax({
                    type: "GET",
                    url: "../mds/entities/" + scope.selectedEntity.id + "/entityFields",
                    dataType: "json",
                    success: function (result) {
                        var colModel = [], i, noSelectedFields = true, spanText,
                            noSelectedFieldsText = scope.msg('mds.dataBrowsing.noSelectedFieldsInfo');

                        buildGridColModel(colModel, result, scope, false);

                        elem.jqGrid({
                            url: "../prevac/instances/" + scope.selectedEntity.name,
                            headers: {
                                'Accept': 'application/x-www-form-urlencoded',
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            datatype: 'json',
                            mtype: "POST",
                            postData: {
                                fields: JSON.stringify(scope.lookupBy)
                            },
                            rowNum: scope.entityAdvanced.userPreferences.gridRowsNumber,
                            onPaging: function (pgButton) {
                                handleGridPagination(pgButton, $(this.p.pager), scope);
                            },
                            jsonReader: {
                                repeatitems: false
                            },
                            prmNames: {
                                sort: 'sortColumn',
                                order: 'sortDirection'
                            },
                            onSelectRow: function (id) {
                                firstLoad = true;
                                scope.editInstance(id, scope.selectedEntity.module, scope.selectedEntity.name);
                            },
                            resizeStop: function (width, index) {
                                var widthNew, widthOrg, colModel = $('#' + gridId).jqGrid('getGridParam','colModel');
                                if (colModel.length - 1 === index + 1 || (colModel[index + 1] !== undefined && isLastNextColumn(colModel, index))) {
                                    widthOrg = colModel[index].widthOrg;
                                    if (Math.floor(width) > Math.floor(widthOrg)) {
                                        widthNew = colModel[index + 1].width + Math.floor(width - widthOrg);
                                        colModel[index + 1].width = widthNew;

                                        $('.ui-jqgrid-labels > th:eq('+(index + 1)+')').css('width', widthNew);
                                        $('#' + gridId + ' .jqgfirstrow > td:eq('+(index + 1)+')').css('width', widthNew);
                                    }
                                    colModel[index].widthOrg = width;
                                }
                                tableWidth = $('#entityInstancesTable').width();
                                $('#' + gridId).jqGrid("setGridWidth", tableWidth);
                            },
                            loadonce: false,
                            headertitles: true,
                            colModel: colModel,
                            pager: '#' + attrs.prevacInstancesGrid,
                            viewrecords: true,
                            autowidth: true,
                            shrinkToFit: false,
                            gridComplete: function () {
                                scope.setDataRetrievalError(false);
                                spanText = $('<span>').addClass('ui-jqgrid-status-label ui-jqgrid ui-widget hidden');
                                spanText.append(noSelectedFieldsText).css({padding: '3px 15px'});
                                $('#entityInstancesTable .ui-paging-info').append(spanText);
                                $('.ui-jqgrid-status-label').addClass('hidden');
                                $('#pageInstancesTable_center').addClass('page_instancesTable_center');
                                if (scope.selectedFields !== undefined && scope.selectedFields.length > 0) {
                                    noSelectedFields = false;
                                } else {
                                    noSelectedFields = true;
                                    $('#pageInstancesTable_center').hide();
                                    $('#entityInstancesTable .ui-jqgrid-status-label').removeClass('hidden');
                                }
                                if ($('#instancesTable').getGridParam('records') > 0) {
                                    $('#pageInstancesTable_center').show();
                                    $('#entityInstancesTable .ui-jqgrid-hdiv').show();
                                    $('.jqgfirstrow').css('height','0');
                                } else {
                                    if (noSelectedFields) {
                                        $('#pageInstancesTable_center').hide();
                                        $('#entityInstancesTable .ui-jqgrid-hdiv').hide();
                                    }
                                    $('.jqgfirstrow').css('height','1px');
                                }
                                $('#entityInstancesTable .ui-jqgrid-hdiv').addClass("table-lightblue");
                                $('#entityInstancesTable .ui-jqgrid-btable').addClass("table-lightblue");
                                $timeout(function() {
                                    resizeGridHeight(gridId);
                                    resizeGridWidth(gridId);
                                }, 550);
                                if (firstLoad) {
                                    resizeIfNarrow(gridId);
                                    firstLoad = false;
                                }
                            },
                            loadError: function() {
                                scope.setDataRetrievalError(true);
                            }
                        });

                        scope.$watch("lookupRefresh", function () {
                            $('#' + attrs.id).jqGrid('setGridParam', {
                                page: 1,
                                postData: {
                                    fields: JSON.stringify(scope.lookupBy),
                                    lookup: (scope.selectedLookup) ? scope.selectedLookup.lookupName : "",
                                    filter: (scope.filterBy) ? JSON.stringify(scope.filterBy) : ""
                                }
                            }).trigger('reloadGrid');
                        });

                        elem.on('jqGridSortCol', function (e, fieldName) {
                            // For correct sorting in jqgrid we need to convert back to the original name
                            e.target.p.sortname = backToReservedFieldName(fieldName);
                        });

                        $(window).on('resize', function() {
                            clearTimeout(eventResize);
                            eventResize = $timeout(function() {
                                $(".ui-layout-content").scrollTop(0);
                                resizeGridWidth(gridId);
                                resizeGridHeight(gridId);
                            }, 200);
                        }).trigger('resize');

                        $('#inner-center').on('change', function() {
                            clearTimeout(eventChange);
                            eventChange = $timeout(function() {
                                resizeGridHeight(gridId);
                                resizeGridWidth(gridId);
                            }, 200);
                        });
                    }
                });
            }
        };
    });

}());