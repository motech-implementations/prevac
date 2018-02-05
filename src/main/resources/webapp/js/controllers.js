(function() {
    'use strict';

    var controllers = angular.module('prevac.controllers', []);

    controllers.controller('PrevacUnscheduledVisitCtrl', function ($scope, $timeout, $http, $filter, ScreenedParticipants) {

        $scope.getLookups("../prevac/unscheduledVisits/getLookupsForUnscheduled");

        $scope.participants = ScreenedParticipants.query();

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];

        $scope.newForm = function(type) {
            $scope.form = {};
            $scope.form.type = type;
            $scope.form.dto = {};
        };

        $scope.addUnscheduled = function() {
            $scope.newForm("add");
            $('#unscheduledVisitModal').modal('show');
            $scope.reloadSelects();
        };

        $scope.saveUnscheduledVisit = function(ignoreLimitation) {
            function sendRequest() {
                $http.post('../prevac/unscheduledVisits/new/' + ignoreLimitation, $scope.form.dto)
                    .success(function(data){
                        if (data && (typeof(data) === 'string')) {
                            jConfirm($scope.msg('prevac.uncheduledVisit.confirmMsg', data), $scope.msg('prevac.uncheduledVisit.confirmTitle'),
                                function (response) {
                                    if (response) {
                                        $scope.saveUnscheduledVisit(true);
                                    }
                                });
                        } else {
                            $("#unscheduleVisit").trigger('reloadGrid');
                            $scope.form.updated = data;
                            $scope.form.dto = undefined;
                        }
                    })
                    .error(function(response) {
                        motechAlert('prevac.uncheduledVisit.scheduleError', 'prevac.error', response);
                    });
            }

            if (ignoreLimitation) {
                sendRequest();
            } else {
                var confirmMsg;
                if ($scope.form.type === "add") {
                    confirmMsg = "prevac.uncheduledVisit.confirm.shouldAddVisit";
                } else if ($scope.form.type === "edit") {
                    confirmMsg = "prevac.uncheduledVisit.confirm.shouldUpdateVisit";
                }

                motechConfirm(confirmMsg, "prevac.confirm",
                    function(confirmed) {
                        if (confirmed) {
                            sendRequest();
                        }
                })
            }
        };

        $scope.formIsFilled = function() {
            return $scope.form
                && $scope.form.dto
                && $scope.form.dto.participantId
                && $scope.form.dto.date
                && $scope.form.dto.startTime;
        };

        $scope.reloadSelects = function() {
            $timeout(function() {
                $('#participantSelect').trigger('change');
            });
        };

        $scope.setPrintData = function(document, rowData) {
            $('#versionDate', document).html($filter('date')(new Date(), $scope.cardDateTimeFormat));
            $('#location', document).html(rowData.clinicName);
            $('#subjectId', document).html(rowData.participantId);
            $('#date', document).html($filter('date')($scope.parseDate(rowData.date), $scope.cardDateFormat));
        };

        $scope.printFrom = function(source) {

            if (source === "updated") {
                rowData = $scope.form.updated;
            } else {
                var rowData = jQuery("#unscheduledVisit").jqGrid ('getRowData', source);
            }

            var winPrint = window.open("../prevac/resources/partials/card/unscheduledVisitCard.html");
             if ((!(window.ActiveXObject) && "ActiveXObject" in window) || (navigator.userAgent.indexOf("MSIE") > -1)) {
             	// iexplorer
                 var windowOnload = winPrint.onload || function() {
                    setTimeout(function(){
                        $scope.setPrintData(winPrint.document, rowData);
                        winPrint.focus();
                        winPrint.print();
                    }, 500);
                 };

                 winPrint.onload = new function() { windowOnload(); } ;
             } else {

                winPrint.onload = function() {
                    $scope.setPrintData(winPrint.document, rowData);
                    winPrint.focus();
                    winPrint.print();
                }
             }
        };

        $scope.exportInstance = function() {
                    var sortColumn, sortDirection, url = "../prevac/exportInstances/unscheduledVisits";
                    url = url + "?outputFormat=" + $scope.exportFormat;
                    url = url + "&exportRecords=" + $scope.actualExportRecords;

                    if ($scope.checkboxModel.exportWithFilter === true) {
                        url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                        if ($scope.selectedFilter.startDate) {
                            url = url + "&startDate=" + $scope.selectedFilter.startDate;
                        }

                        if ($scope.selectedFilter.endDate) {
                            url = url + "&endDate=" + $scope.selectedFilter.endDate;
                        }
                    }

                    if ($scope.checkboxModel.exportWithOrder === true) {
                        sortColumn = $('#unscheduledVisit').getGridParam('sortname');
                        sortDirection = $('#unscheduledVisit').getGridParam('sortorder');

                        url = url + "&sortColumn=" + sortColumn;
                        url = url + "&sortDirection=" + sortDirection;
                    }

                    $scope.exportInstanceWithUrl(url);
                };
    });

    controllers.controller('PrevacBaseCtrl', function ($scope, $timeout, $http, MDSUtils) {

        $scope.filters = [{
            name: $scope.msg('prevac.screening.today'),
            dateFilter: "TODAY"
        },{
            name: $scope.msg('prevac.screening.tomorrow'),
            dateFilter: "TOMORROW"
        },{
            name: $scope.msg('prevac.screening.twoDaysAfter'),
            dateFilter: "TWO_DAYS_AFTER"
        },{
            name: $scope.msg('prevac.screening.nextThreeDays'),
            dateFilter: "NEXT_THREE_DAYS"
        },{
            name: $scope.msg('prevac.screening.thisWeek'),
            dateFilter: "THIS_WEEK"
        },{
            name: $scope.msg('prevac.screening.dateRange'),
            dateFilter: "DATE_RANGE"
        }];

        $scope.selectedFilter = $scope.filters[0];

        $scope.selectFilter = function(value) {
            $scope.selectedFilter = $scope.filters[value];
            if (value !== 5) {
                $scope.refreshGrid();
            }
        };

        $scope.cardDateFormat = "dd-MM-yyyy";
        $scope.cardDateTimeFormat = "dd-MM-yyyy HH:mm";

        $scope.availableExportRecords = ['All','10', '25', '50', '100', '250'];
        $scope.availableExportFormats = ['pdf','xls'];
        $scope.actualExportRecords = 'All';
        $scope.actualExportColumns = 'All';
        $scope.exportFormat = 'pdf';
        $scope.checkboxModel = {
            exportWithOrder : false,
            exportWithFilter : true
        };

        $scope.exportEntityInstances = function () {
            $scope.checkboxModel.exportWithFilter = true;
            $('#exportPrevacInstanceModal').modal('show');
        };

        $scope.changeExportRecords = function (records) {
            $scope.actualExportRecords = records;
        };

        $scope.changeExportFormat = function (format) {
            $scope.exportFormat = format;
        };

        $scope.closeExportPrevacInstanceModal = function () {
            $('#exportPrevacInstanceModal').resetForm();
            $('#exportPrevacInstanceModal').modal('hide');
        };

        $scope.exportInstanceWithUrl = function(url) {
            if ($scope.selectedLookup !== undefined && $scope.checkboxModel.exportWithFilter === true) {
                url = url + "&lookup=" + (($scope.selectedLookup) ? $scope.selectedLookup.lookupName : "");
                url = url + "&fields=" + encodeURIComponent(JSON.stringify($scope.lookupBy));
            }

            $http.get(url)
            .success(function () {
                $('#exportPrevacInstanceModal').resetForm();
                $('#exportPrevacInstanceModal').modal('hide');
                window.location.replace(url);
            })
            .error(function (response) {
                handleResponse('mds.error', 'mds.error.exportData', response);
            });
        };

        $scope.screeningForPrint = {};

        $scope.parseTime = function(string) {

            if (string === undefined || string === null || string === "") {
                return string;
            }

            var split = string.split(":"),
                time = {};

            time.hours = parseInt(split[0]);
            time.minutes = parseInt(split[1]);

            return time;
        };

        $scope.parseDate = function(date, offset) {
            if (date !== undefined && date !== null) {
                var parts = date.split('-'), date;

                if (offset) {
                    date = new Date(parts[0], parts[1] - 1, parseInt(parts[2]) + offset);
                } else {
                    date = new Date(parts[0], parts[1] - 1, parts[2]);
                }
                return date;
            }
            return undefined;
        };

        $scope.isValidEndTime = function(startTimeString, endTimeString) {

            var startTime = $scope.parseTime(startTimeString),
                endTime = $scope.parseTime(endTimeString);

            if (startTime === undefined || startTime === null || endTime === undefined || endTime === null) {
                return undefined;
            }

            if (endTime === "") {
                return false;
            }

            if (startTime.hours === endTime.hours) {
                return startTime.minutes < endTime.minutes;
            }

            return startTime.hours < endTime.hours;
        };

        $scope.lookupBy = {};
        $scope.selectedLookup = undefined;
        $scope.lookupFields = [];

        $scope.getLookups = function(url) {
            $scope.lookupBy = {};
            $scope.selectedLookup = undefined;
            $scope.lookupFields = [];

            $http.get(url)
            .success(function(data) {
                $scope.lookups = data;
            });
        };

        /**
        * Shows/Hides lookup dialog
        */
        $scope.showLookupDialog = function() {
            $("#lookup-dialog")
            .css({'top': ($("#lookupDialogButton").offset().top - $("#main-content").offset().top) - 40,
            'left': ($("#lookupDialogButton").offset().left - $("#main-content").offset().left) - 15})
            .toggle();
            $("div.arrow").css({'left': 50});
        };

        $scope.hideLookupDialog = function() {
            $("#lookup-dialog").hide();
        };

        /**
        * Marks passed lookup as selected. Sets fields that belong to the given lookup and resets lookupBy object
        * used to filter instances by given values
        */
        $scope.selectLookup = function(lookup) {
            $scope.selectedLookup = lookup;
            $scope.lookupFields = lookup.lookupFields;
            $scope.lookupBy = {};
        };

        /**
        * Removes lookup and resets all fields associated with a lookup
        */
        $scope.removeLookup = function() {
            $scope.lookupBy = {};
            $scope.selectedLookup = undefined;
            $scope.lookupFields = [];
            $scope.filterInstancesByLookup();
        };

        /**
        * Hides lookup dialog and sends signal to refresh the grid with new data
        */
        $scope.filterInstancesByLookup = function() {
            $scope.showLookupDialog();
            $scope.refreshGrid();
        };

        $scope.refreshGrid = function() {
            $scope.lookupRefresh = !$scope.lookupRefresh;
        };

        $scope.buildLookupFieldName = function (field) {
            if (field.relatedName !== undefined && field.relatedName !== '' && field.relatedName !== null) {
                return field.name + "." + field.relatedName;
            }
            return field.name;
        };

        /**
        * Depending on the field type, includes proper html file containing visual representation for
        * the object type. Radio input for boolean, select input for list and text input as default one.
        */
        $scope.loadInputForLookupField = function(field) {
            var value = "default", type = "field";

            if (field.className === "java.lang.Boolean") {
                value = "boolean";
            } else if (field.className === "java.util.Collection") {
                value = "list";
            } else if (field.className === "org.joda.time.DateTime" || field.className === "java.util.Date") {
                value = "datetime";
            } else if (field.className === "org.joda.time.LocalDate") {
                value = "date";
            }

            if ($scope.isRangedLookup(field)) {
                type = "range";
                if (!$scope.lookupBy[$scope.buildLookupFieldName(field)]) {
                    $scope.lookupBy[$scope.buildLookupFieldName(field)] = {min: '', max: ''};
                }
            }

            return '../prevac/resources/partials/lookups/{0}-{1}.html'.format(type, value);
        };

        $scope.isRangedLookup = function(field) {
            return $scope.isLookupFieldOfType(field, 'RANGE');
        };

        $scope.isLookupFieldOfType = function(field, type) {
            var i, lookupField;
            for (i = 0; i < $scope.selectedLookup.lookupFields.length; i += 1) {
                lookupField = $scope.selectedLookup.lookupFields[i];
                if ($scope.buildLookupFieldName(lookupField) === $scope.buildLookupFieldName(field)) {
                    return lookupField.type === type;
                }
            }
        };

        $scope.getComboboxValues = function (settings) {
            var labelValues = MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.values'}], true).value, keys = [], key;
            // Check the user supplied flag, if true return string set
            if (MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.allowUserSupplied'}], true).value === true){
                return labelValues;
            } else {
                if (labelValues !== undefined && labelValues[0].indexOf(":") !== -1) {
                    labelValues =  $scope.getAndSplitComboboxValues(labelValues);
                    for(key in labelValues) {
                        keys.push(key);
                    }
                    return keys;
                } else {        // there is no colon, so we are dealing with a string set, not a map
                    return labelValues;
                }
            }
        };

        $scope.getComboboxDisplayName = function (settings, value) {
            var labelValues = MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.values'}], true).value;
            // Check the user supplied flag, if true return string set
            if (MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.allowUserSupplied'}], true).value === true){
                return value;
            } else {
                if (labelValues[0].indexOf(":") === -1) { // there is no colon, so we are dealing with a string set, not a map
                    return value;
                } else {
                    labelValues =  $scope.getAndSplitComboboxValues(labelValues);
                    return labelValues[value];
                }
            }

        };

        $scope.getAndSplitComboboxValues = function (labelValues) {
            var doublet, i, map = {};
            for (i = 0; i < labelValues.length; i += 1) {
                doublet = labelValues[i].split(":");
                map[doublet[0]] = doublet[1];
            }
            return map;
        };

        $scope.resizeGridHeight = function(gridId) {
            var intervalHeightResize, gap, tableHeight;
            clearInterval(intervalHeightResize);
            intervalHeightResize = setInterval( function () {
                if ($('.overrideJqgridTable').offset() !== undefined) {
                    gap = 1 + $('.overrideJqgridTable').offset().top - $('.inner-center .ui-layout-content').offset().top;
                    tableHeight = Math.floor($('.inner-center .ui-layout-content').height() - gap - $('.ui-jqgrid-pager').outerHeight() - $('.ui-jqgrid-hdiv').outerHeight());
                    $('#' + gridId).jqGrid("setGridHeight", tableHeight);
                }
                clearInterval(intervalHeightResize);
            }, 250);
         };

        $scope.resizeGridWidth = function(gridId) {
            var intervalWidthResize, tableWidth;
            clearInterval(intervalWidthResize);
            intervalWidthResize = setInterval( function () {
                tableWidth = $('.overrideJqgridTable').width();
                $('#' + gridId).jqGrid("setGridWidth", tableWidth);
                clearInterval(intervalWidthResize);
            }, 250);
        }
    });

    controllers.controller('PrevacSettingsCtrl', function ($scope, $http, $timeout) {
        $scope.errors = [];
        $scope.messages = [];

        $scope.clinicFields = [];
        $scope.availableMainFields = [];
        $scope.availableExtendedFields = [];

        $scope.availableVisits = [];

        $scope.mainFieldsChanged = function(change) {
            var value;

            if (change.added) {
                value = change.added.text;
                $scope.config.clinicMainFields.push(value);
                $scope.availableExtendedFields.removeObject(value);
            } else if (change.removed) {
                value = change.removed.text;
                $scope.config.clinicMainFields.removeObject(value);
                $scope.availableExtendedFields.push(value);
            }

            $scope.updateAvailableClinicFields();
            $scope.$apply();
        };

        $scope.extendedFieldsChanged = function(change) {
            var value;

            if (change.added) {
                value = change.added.text;
                $scope.config.clinicExtendedFields.push(value);
                $scope.availableMainFields.removeObject(value);
            } else if (change.removed) {
                value = change.removed.text;
                $scope.config.clinicExtendedFields.removeObject(value);
                $scope.availableMainFields.push(value);
            }

            $scope.updateAvailableClinicFields();
            $scope.$apply();
        };

        $scope.updateAvailableClinicFields = function() {
            $scope.availableMainFields = $scope.clinicFields.filter(function(el) {
               return $scope.config.clinicExtendedFields.indexOf(el) < 0;
            });

            $scope.availableExtendedFields = $scope.clinicFields.filter(function(el) {
               return $scope.config.clinicMainFields.indexOf(el) < 0;
            });
        };

        $scope.boostRelVisitsChanged = function(change) {
            var value;

            if (change.added) {
                value = change.added.text;
                $scope.config.boosterRelatedVisits.push(value);
            } else if (change.removed) {
                value = change.removed.text;
                $scope.config.boosterRelatedVisits.removeObject(value);
            }
        };

        $http.get('../prevac/prevac-config')
            .success(function(response){
                var i;
                $scope.config = response;
                $scope.originalConfig = angular.copy($scope.config);

                $http.get('../prevac/clinicFields')
                    .success(function(response) {
                        $scope.clinicFields = response;
                        $scope.updateAvailableClinicFields();

                        $timeout(function() {
                            $('#clinicMainFields').select2('val', $scope.config.clinicMainFields);
                            $('#clinicExtendedFields').select2('val', $scope.config.clinicExtendedFields);
                        }, 50);

                    })
                    .error(function(response) {
                        $scope.errors.push($scope.msg('prevac.settings.advancedSettings.clinicFields.error', response));
                    });

                $http.get('../prevac/availableVisits')
                    .success(function(response){
                        $scope.availableVisits = response;
                        $timeout(function() {
                            $('#boostRelVisits').select2('val', $scope.config.boosterRelatedVisits);
                        }, 50);

                    })
                    .error(function(response) {
                        $scope.errors.push($scope.msg('prevac.settings.enroll.disconVacCampaigns.error', response));
                    });
            })
            .error(function(response) {
                $scope.errors.push($scope.msg('prevac.settings.noConfig', response));
            });

        $scope.reset = function () {
            $scope.config = angular.copy($scope.originalConfig);
            $scope.updateAvailableClinicFields();
            $timeout(function() {
                $('#clinicMainFields').select2('val', $scope.config.clinicMainFields);
                $('#clinicExtendedFields').select2('val', $scope.config.clinicExtendedFields);
            }, 50);

            $('#boostRelVisits').select2('val', $scope.config.boosterRelatedVisits);
        };

        function hideMsgLater(index) {
            return $timeout(function() {
                $scope.messages.splice(index, 1);
            }, 5000);
        }

        $scope.submit = function () {
            $http.post('../prevac/prevac-config', $scope.config)
                .success(function (response) {
                    $scope.config = response;
                    $scope.originalConfig = angular.copy($scope.config);
                    var index = $scope.messages.push($scope.msg('prevac.settings.saved'));
                    hideMsgLater(index-1);
                })
                .error (function (response) {
                    //todo: better than that!
                    handleWithStackTrace('prevac.error.header', 'prevac.error.body', response);
                });
        };
    });

    controllers.controller('PrevacScreeningCtrl', function ($scope, $timeout, $http, $filter, Screenings, Clinics) {

        $scope.getLookups("../prevac/screenings/getLookupsForScreening");

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];

        $scope.clinics = Clinics.query()
            .$promise.then(
                //success
                function(clinics) {
                    if (clinics.length === 1
                        && $scope.form !== undefined && $scope.form !== null
                        && $scope.form.dto !== undefined && $scope.form.dto !== null) {
                        $scope.form.dto.clinicLocation = clinics[0].location;
                        $scope.form.dto.clinicId = clinics[0].id;
                    }
                    $scope.clinics = clinics;
                },
                //error
                function(response) {
                    $scope.clinics = [];
                    motechAlert('prevac.screening.clinicsLoading', 'prevac.error', response);
                });

        $scope.updateInProgress = false;

        $scope.cancel = function(id) {
            $scope.updateInProgress = true;

            motechConfirm("prevac.screening.confirm.cancel", "prevac.confirm", function(confirmed) {
                if (confirmed) {
                    $http.post('../prevac/screenings/cancel/', id)
                        .success(function(data) {
                            $("#screenings").trigger('reloadGrid');
                            $scope.updateInProgress = false;
                        })
                        .error(function(response) {
                            motechAlert('prevac.screening.updateError', 'prevac.error', response);
                            $scope.updateInProgress = false;
                        });
                } else {
                    $scope.updateInProgress = false;
                    $scope.$apply();
                }
            });
        };

        $scope.activate = function(id) {
            $scope.updateInProgress = true;

            function sendRequest(ignoreLimitation) {
                $http.post('../prevac/screenings/activate/' + ignoreLimitation, id)
                    .success(function(data) {
                        if (data !== null && data !== undefined && data !== '') {
                            jConfirm($scope.msg('prevac.screening.confirmMsg', data), $scope.msg('prevac.confirm'),
                                function (confirmed) {
                                    if (confirmed) {
                                        sendRequest(true);
                                    } else {
                                        $scope.updateInProgress = false;
                                        $scope.$apply();
                                    }
                                });
                        } else {
                            $("#screenings").trigger('reloadGrid');
                            $scope.updateInProgress = false;
                        }
                    })
                    .error(function(response) {
                        motechAlert('prevac.screening.updateError', 'prevac.error', response);
                        $scope.updateInProgress = false;
                    });
            }

            motechConfirm("prevac.screening.confirm.activate", "prevac.confirm", function(confirmed) {
                if (confirmed) {
                    sendRequest(false);
                } else {
                    $scope.updateInProgress = false;
                    $scope.$apply();
                }
            });
        };

        $scope.newForm = function(type) {
            $scope.form = {};
            $scope.form.type = type;
            $scope.form.dto = {};
            if ($scope.clinics.length === 1) {
                $scope.form.dto.clinicLocation = $scope.clinics[0].location;
                $scope.form.dto.clinicId = $scope.clinics[0].id;
            }
        };

        $scope.showClinicDropdown = function () {
          return $scope.clinics.length > 1;
        };

        $scope.reloadSelects = function() {
            $timeout(function() {
                $('#clinicSelect').trigger('change');
            });
        };

        $scope.addScreening = function() {
            $scope.newForm("add");
            $('#screeningModal').modal('show');
            $scope.reloadSelects();
        };

        $scope.editScreening = function(id) {
            $scope.newForm("edit");
            $scope.form.dto = Screenings.get({id: id}, function() {
                $scope.reloadSelects();
                $('#screeningModal').modal('show');
            });
        };

        $scope.saveScreening = function(ignoreLimitation) {
            var confirmMsg;

            function sendRequest() {
                $http.post('../prevac/screenings/new/' + ignoreLimitation, $scope.form.dto)
                    .success(function(data) {
                        if (data && (typeof(data) === 'string')) {
                            jConfirm($scope.msg('prevac.screening.confirmMsg', data), $scope.msg('prevac.screening.confirmTitle'),
                                function (response) {
                                    if (response) {
                                        $scope.saveScreening(true);
                                    }
                                });
                        } else {
                            $("#screenings").trigger('reloadGrid');
                            $scope.screeningForPrint = data;
                            $scope.form.dto = undefined;
                        }
                    })
                    .error(function(response) {
                        motechAlert('prevac.screening.scheduleError', 'prevac.error', response);
                    });
            }

            if ($scope.form.type === "add") {
                confirmMsg = "prevac.screening.confirm.shouldScheduleScreening";
            } else if ($scope.form.type === "edit") {
                confirmMsg = "prevac.screening.confirm.shouldUpdateScreening";
            }

            if (ignoreLimitation) {
                sendRequest();
            } else {
                motechConfirm(confirmMsg, "prevac.confirm", function(confirmed) {
                    if (confirmed) {
                        sendRequest();
                    }
                });
            }
        };

        $scope.formIsFilled = function() {
            return $scope.form
                && $scope.form.dto
                && $scope.form.dto.date
                && $scope.form.dto.clinicId
                && $scope.form.dto.name;
        };

        $scope.exportInstance = function() {
            var sortColumn, sortDirection, url = "../prevac/exportInstances/screening";
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithFilter === true) {
                url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                if ($scope.selectedFilter.startDate) {
                    url = url + "&startDate=" + $scope.selectedFilter.startDate;
                }

                if ($scope.selectedFilter.endDate) {
                    url = url + "&endDate=" + $scope.selectedFilter.endDate;
                }
            }

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#screenings').getGridParam('sortname');
                sortDirection = $('#screenings').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            $scope.exportInstanceWithUrl(url);
        };

        $scope.setPrintData = function(document, bookingId, date, location) {

            $('#versionDate', document).html($filter('date')(new Date(), $scope.cardDateTimeFormat));
            $('#bookingId', document).html(bookingId);
            $('#screeningDate', document).html($filter('date')($scope.parseDate(date), $scope.cardDateFormat));
            $('#location', document).html(location);
        };

        $scope.printRow = function(id) {

            if(id >= 0) {
                var rowData = jQuery("#screenings").jqGrid ('getRowData', id);
                var bookingId = rowData['volunteer.id'];
                var date = rowData['date'];
                var location = rowData['clinic.location']
            } else {
                var bookingId = $scope.screeningForPrint.volunteer.id;
                var date = $scope.screeningForPrint.date;
                var location = $scope.screeningForPrint.clinic.location;
            }

            var winPrint = window.open("../prevac/resources/partials/card/screeningCard.html");
            if ((!(window.ActiveXObject) && "ActiveXObject" in window) || (navigator.userAgent.indexOf("MSIE") > -1)) {
                // iexplorer
                var windowOnload = winPrint.onload || function() {
                	setTimeout(function(){
                        $scope.setPrintData(winPrint.document, bookingId, date, location);
                        winPrint.focus();
                        winPrint.print();
                    }, 500);
                };
                winPrint.onload = new function() { windowOnload(); } ;
            } else {
                winPrint.onload = function() {
                    $scope.setPrintData(winPrint.document, bookingId, date, location);
                    winPrint.focus();
                    winPrint.print();
                }
            }
        }
    });

    controllers.controller('PrevacPrimeVaccinationCtrl', function ($scope, $timeout, $http, $filter) {

        $scope.getLookups("../prevac/getLookupsForPrimeVaccinationSchedule");

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];

        $scope.form = {};
        $scope.form.dto = undefined;

        $scope.primeVacDtos = [];
        $scope.participantsLoading = false;

        $scope.getPrimeVacDtos = function() {
            $scope.participantsLoading = true;
            $http.get('../prevac/getPrimeVacDtos')
            .success(function(data) {
                $scope.primeVacDtos = data;
                $scope.participantsLoading = false;
            })
            .error(function(response) {
                motechAlert('prevac.primeVaccination.getParticipantsError', 'prevac.error', response);
                $scope.participantsLoading = false;
            });
        };

        $scope.getPrimeVacDtos();

        $scope.newForm = function(type) {
            if ($scope.form.dto) {
                $scope.form.dto.actualScreeningDate = null;
                $scope.form.dto.date = null;
                $scope.form.dto.startTime = null;
                $scope.form.dto.ignoreDateLimitation = false;
                $('#primeVacTimeInput').datetimepicker("setTime", "00:00");
            }
            $scope.form = {};
            $scope.form.dto = {};
            $scope.form.type = type;
        };

        $scope.addPrimeVaccination = function() {
            $scope.newForm("add");
            $timeout(function() {
                $('#subjectIdSelect').trigger('change');
            });
            $('#primeVaccinationScheduleModal').modal('show');
        };

        $scope.subjectChanged = function() {
            $scope.reloadSelects();
            if ($scope.form.dto) {
                $scope.form.range = $scope.calculateRange($scope.form.dto.actualScreeningDate, $scope.form.dto.femaleChildBearingAge, $scope.form.dto.ignoreDateLimitation);
            }
        };

        $scope.savePrimeVaccinationSchedule = function(ignoreLimitation) {

            function sendRequest() {
                if ($scope.form.dto.participantGender === "Male" || $scope.form.dto.participantGender === "Unknown" || $scope.form.dto.participantGender === "Unidentified") {
                    $scope.form.dto.femaleChildBearingAge = "No";
                }
                $http.post('../prevac/primeVaccinationSchedule/' + ignoreLimitation, $scope.form.dto)
                    .success(function(data) {
                        if (data && (typeof(data) === 'string')) {
                            jConfirm($scope.msg('prevac.primeVaccination.confirmMsg', data), $scope.msg('prevac.primeVaccination.confirmTitle'),
                                function (response) {
                                    if (response) {
                                        $scope.savePrimeVaccinationSchedule(true);
                                    }
                                });
                        } else {
                            $("#primeVaccinationSchedule").trigger('reloadGrid');
                            $scope.primeVacDtos = [];
                            $scope.getPrimeVacDtos();
                            $scope.form.updated = data;
                            $scope.form.dto = undefined;
                        }
                    })
                    .error(function(response) {
                        motechAlert('prevac.primeVaccination.updateError', 'prevac.error', response);
                    });
            }

            if (ignoreLimitation) {
                sendRequest();
            } else {
                var confirmMsg;
                if ($scope.form.type === "add") {
                    confirmMsg  = "prevac.primeVaccination.confirm.shouldCreatePrimeVaccination";                   
                } else if ($scope.form.type === "edit") {
                    confirmMsg  = "prevac.primeVaccination.confirm.shouldUpdatePrimeVaccination";
                }
                motechConfirm(confirmMsg, "prevac.confirm", function(confirmed) {
                        if (confirmed) {
                            sendRequest();
                        }
                    })
            }
        };

        $scope.reloadSelects = function() {
            $timeout(function() {
                $('#femaleChildBearingAgeSelect').trigger('change');
            }, 100);
        };

        $scope.calculateRangeForGrid = function(forDate, femaleChildBearingAge, ignoreDateLimitation) {
            var range = {};

            if (ignoreDateLimitation === undefined || ignoreDateLimitation === '' || ignoreDateLimitation === null || ignoreDateLimitation === false) {
                if (femaleChildBearingAge === "Yes") {
                    range.min = $scope.parseDate(forDate, 14);
                } else {
                    range.min = $scope.parseDate(forDate, 1);
                }

                range.max = $scope.parseDate(forDate, 28);
            } else {
                range.min = new Date();
                range.max = null;
            }

            return range;
        };

        $scope.calculateRange = function(forDate, femaleChildBearingAge, ignoreDateLimitation) {
            var range = $scope.calculateRangeForGrid(forDate, femaleChildBearingAge, ignoreDateLimitation);
            var today = new Date();

            if (today > range.min) {
                range.min = today;
            }

            return range;
        };

        $scope.$watch('form.dto.femaleChildBearingAge', function (value) {
            if ($scope.form.dto) {
                $scope.form.range = $scope.calculateRange($scope.form.dto.actualScreeningDate, value, $scope.form.dto.ignoreDateLimitation);
            }
        });

        $scope.$watch('form.dto.actualScreeningDate', function (value) {
            if ($scope.form.dto) {
                $scope.form.range = $scope.calculateRange(value, $scope.form.dto.femaleChildBearingAge, $scope.form.dto.ignoreDateLimitation);
            }
        });

        $scope.$watch('form.dto.ignoreDateLimitation', function (value) {
            if ($scope.form.dto) {
                $scope.form.range = $scope.calculateRange($scope.form.dto.actualScreeningDate, $scope.form.dto.femaleChildBearingAge, value);
            }
        });

        $scope.formIsFilled = function() {
            return $scope.form
                && $scope.form.dto
                && $scope.form.dto.participantId
                && $scope.form.dto.date
                && $scope.form.dto.startTime
                && $scope.form.dto.femaleChildBearingAge
                && $scope.form.dto.actualScreeningDate;
        };

        $scope.exportInstance = function() {
            var sortColumn, sortDirection, url = "../prevac/exportInstances/primeVaccinationSchedule";
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithFilter === true) {
                url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                if ($scope.selectedFilter.startDate) {
                    url = url + "&startDate=" + $scope.selectedFilter.startDate;
                }

                if ($scope.selectedFilter.endDate) {
                    url = url + "&endDate=" + $scope.selectedFilter.endDate;
                }
            }

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#primeVaccinationSchedule').getGridParam('sortname');
                sortDirection = $('#primeVaccinationSchedule').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            $scope.exportInstanceWithUrl(url);
        };

        $scope.setPrintData = function(document, rowData) {

            $('#versionDate', document).html($filter('date')(new Date(), $scope.cardDateTimeFormat));
            $('#location', document).html(rowData.location);
            $('#participantId', document).html(rowData.participantId);
            $('#name', document).html(rowData.participantName);
            $('#primeVaccinationDate', document).html($filter('date')($scope.parseDate(rowData.date), $scope.cardDateFormat));
            $('#appointmentTime', document).html(rowData.startTime);
            $('#location', document).html(rowData.location);
        };

        $scope.printCardFrom = function(source) {

            var rowData;

            if (source === "updated") {
                rowData = $scope.form.updated;
            } else {
                rowData = $("#primeVaccinationSchedule").getRowData(source);
            }

            var winPrint = window.open("../prevac/resources/partials/card/primeVaccinationCard.html");
            if ((!(window.ActiveXObject) && "ActiveXObject" in window) || (navigator.userAgent.indexOf("MSIE") > -1)) {
                // iexplorer
                 var windowOnload = winPrint.onload || function() {
                    setTimeout(function(){
                        $scope.setPrintData(winPrint.document, rowData);
                        winPrint.focus();
                        winPrint.print();
                    }, 500);
                  };

                  winPrint.onload = new function() { windowOnload(); } ;
            } else {
                winPrint.onload = function() {
                    $scope.setPrintData(winPrint.document, rowData);
                    winPrint.focus();
                    winPrint.print();
                }
            }
        };
    });

    controllers.controller('PrevacClinicVisitScheduleCtrl', function ($scope, $http, $filter, $timeout) {
        $scope.screeningVisits = [];
        $scope.selectedSubject = {};
        $scope.primeVac = {};
        $scope.visitPlannedDates = {};
        $scope.nextVisit = "";

        $http.get('../prevac/schedule/getScreeningVisits')
        .success(function(data) {
            $scope.screeningVisits = data;
        });

        $scope.subjectChanged = function() {
            if ($scope.checkSubject()) {
                $http.get('../prevac/schedule/getPrimeVacDate/' + $scope.selectedSubject.subjectId)
                .success(function(data) {
                    $timeout(function() {
                        $('#primeVacDateInput').val(data.primeVacDate);
                        }, 1);
                    $scope.primeVac.date = data.primeVacDate;
                    $scope.dateRange = {};
                    $scope.dateRange.min = $scope.parseDate(data.earliestDate);
                    $scope.dateRange.max = $scope.parseDate(data.latestDate);
                })
                .error(function(response) {
                    motechAlert('prevac.schedule.plannedDates.calculate.error', 'prevac.schedule.error', response);
                });
            }
        };

        $scope.$watch('primeVac.date', function(newVal, oldVal) {
            if ($scope.checkSubject()) {
                $http.get('../prevac/schedule/getPlannedDates/' + $scope.selectedSubject.subjectId + '/' + newVal)
                .success(function(data) {
                    $scope.visitPlannedDates = data;
                    var nextVisit = $scope.findNextVisit();
                    if (nextVisit === null) {
                        $scope.nextVisit = '';
                    } else {
                         $scope.nextVisit = nextVisit + ": " + $scope.visitPlannedDates[nextVisit];
                    }
                })
                .error(function(response) {
                    motechAlert('prevac.schedule.plannedDates.calculate.error', 'prevac.schedule.error', response);
                });
            }
        });

        $scope.save = function() {
            var confMessage = "prevac.schedule.confirm.shouldSaveDates";
            if ($scope.selectedSubject.primerVaccinationDate !== null) {
                confMessage = "prevac.schedule.confirm.shouldUpdateDates";
            }

            motechConfirm(confMessage, "prevac.confirm", function(confirmed) {
                if (confirmed) {
                    var date, now;
                    now = new Date();
                    date = $scope.parseDate($scope.primeVac.date);
                    date.setHours(23,59,59,0);
                    if (date < now) {
                        confMessage = "prevac.schedule.confirm.shouldSavePastDates";
                        motechConfirm(confMessage, "prevac.confirm", function(confirmed) {
                            if (confirmed) {
                                $scope.saveVisits();
                            }
                        });
                    } else {
                        $scope.saveVisits();
                    }
                }
            });
        };

        $scope.saveVisits = function () {
            if ($scope.checkSubjectAndPrimeVacDate()) {
                $http.get('../prevac/schedule/savePlannedDates/' + $scope.selectedSubject.subjectId + '/' + $scope.primeVac.date)
                    .success(function(response) {
                        motechAlert('prevac.schedule.plannedDates.saved', 'prevac.schedule.saved.success');
                    })
                    .error(function(response) {
                        motechAlert('prevac.schedule.plannedDates.save.error', 'prevac.schedule.error', response);
                    });
            }
        };

        $scope.setPrintData = function(document) {

            $('#versionDate', document).html($filter('date')(new Date(), $scope.cardDateTimeFormat));
            $('#subjectId', document).html($scope.selectedSubject.subjectId);
            $('#subjectName', document).html($scope.selectedSubject.name);
            $('#nextVisit', document).html($filter('date')($scope.parseDate($scope.visitPlannedDates[$scope.findNextVisit()]), $scope.cardDateFormat));
            $('#location', document).html($scope.selectedSubject.siteName);
        };


        $scope.findNextVisit = function () {
            var currentDate = new Date();
            var nextVisitDate = null;
            var nextVisit = null;

            for (var key in $scope.visitPlannedDates) {
                if ($scope.visitPlannedDates.hasOwnProperty(key)) {
                    var visitDate = $scope.parseDate($scope.visitPlannedDates[key]);
                    if (currentDate <= visitDate && (nextVisitDate === null || visitDate < nextVisitDate)) {
                        nextVisitDate = visitDate;
                        nextVisit = key;
                    }
                }
            }

            return nextVisit;
        };

        $scope.print = function() {
            if ($scope.checkSubjectAndPrimeVacDate()) {
                var winPrint = window.open("../prevac/resources/partials/card/visitScheduleCard.html");
                if ((!(window.ActiveXObject) && "ActiveXObject" in window) || (navigator.userAgent.indexOf("MSIE") > -1)) {
                    // iexplorer
                     var windowOnload = winPrint.onload || function() {
                        setTimeout(function(){
                            $scope.setPrintData(winPrint.document);
                            winPrint.focus();
                            winPrint.print();
                        }, 500);
                     };

                     winPrint.onload = new function() { windowOnload(); } ;
                } else {
                    winPrint.onload = function() {
                        $scope.setPrintData(winPrint.document);
                        winPrint.focus();
                        winPrint.print();
                    }
                }
            }
        };

        $scope.cancel = function() {
            $scope.subjectChanged();
        };

        $scope.checkSubject = function() {
            return $scope.selectedSubject !== undefined && $scope.selectedSubject !== null && $scope.selectedSubject.subjectId !== undefined;
        };

        $scope.checkSubjectAndPrimeVacDate = function() {
            return $scope.checkSubject() && $scope.primeVac.date !== undefined && $scope.primeVac.date !== null && $scope.primeVac.date !== "";
        };

        $scope.setPrimeVacDateToCurrentDate = function () {
            $scope.primeVac.date = $filter('date')(new Date(), "yyyy-MM-dd");
        };

        $scope.isTodayButtonDisabled =  function () {
            var currentDate = new Date();
            currentDate.setHours(0, 0, 0, 0);
            if ($scope.dateRange !== undefined && $scope.dateRange !== null) {
                return currentDate < $scope.dateRange.min || currentDate > $scope.dateRange.max;
            }
            return true;
        }
    });

    controllers.controller('PrevacRescheduleCtrl', function ($scope, $http, $timeout, $filter) {
        $scope.getLookups("../prevac/getLookupsForVisitReschedule");

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[5];
        $scope.visitForPrint = {};

        $scope.newForm = function() {
            $scope.form = {};
            $scope.form.dto = {};
        };

        $scope.setActualDateToCurrentDate = function () {
            $scope.form.dto.actualDate = $filter('date')(new Date(), "yyyy-MM-dd");
        };

        $scope.showPlannedDate = function () {
            var isActualDateEmpty = $scope.form.dto.actualDate === null || $scope.form.dto.actualDate === "" || $scope.form.dto.actualDate === undefined;
            var currentDate = new Date();
            currentDate.setHours(0,0,0,0);
            return isActualDateEmpty && ($scope.form.dto.maxDate >= currentDate || $scope.form.dto.ignoreDateLimitation);
        };

        $scope.clearActualDate = function () {
           motechConfirm("prevac.visitReschedule.removeActualDate", "prevac.confirm", function(confirmed) {
                   if (confirmed) {
                       $scope.form.dto.actualDate = null;
                       $timeout(function() {
                           $('#actualDateInput').trigger('change');
                       }, 100);
                   }
           })
        };

        $scope.showRescheduleModal = function(modalHeaderMessage, modalBodyMessage) {
            $timeout(function() {
            $scope.rescheduleModalHeader = modalHeaderMessage;
            $scope.rescheduleModalBody = modalBodyMessage;
            $('#visitRescheduleModal').modal('show');
            $scope.setDatePicker();
            }, 10);
        };

        $scope.setDatePicker = function () {
            var plannedDate = $scope.parseDate($scope.form.dto.plannedDate);
            $('#plannedDateInput').datepicker("setDate", plannedDate);
            $('#plannedDateInput').datepicker('option', 'minDate', $scope.form.dto.minDate);
            $('#plannedDateInput').datepicker('option', 'maxDate', $scope.form.dto.maxDate);

            var actualDate = $scope.parseDate($scope.form.dto.actualDate);
            $('#actualDateInput').datepicker("setDate", actualDate);
            $('#actualDateInput').datepicker('option', 'minDate', $scope.form.dto.minActualDate);
            $('#actualDateInput').datepicker('option', 'maxDate', $scope.form.dto.maxActualDate);
        }

        $scope.saveVisitReschedule = function(ignoreLimitation) {
            function sendRequest() {
                $http.post('../prevac/saveVisitReschedule/' + ignoreLimitation, $scope.form.dto)
                    .success(function(data) {
                        if (data && (typeof(data) === 'string')) {
                            jConfirm($scope.msg('prevac.visitReschedule.confirmMsg', data), $scope.msg('prevac.visitReschedule.confirmTitle'),
                                function (response) {
                                    if (response) {
                                        $scope.saveVisitReschedule(true);
                                    }
                                });
                        } else {
                            $("#visitReschedule").trigger('reloadGrid');
                            $scope.visitForPrint = data;
                            $scope.form.dto = undefined;
                        }
                    })
                    .error(function(response) {
                        motechAlert('prevac.visitReschedule.updateError', 'prevac.error', response);
                    });
            }

            if ($scope.form.dto.startTime === "") {
                $scope.form.dto.startTime = null;
            }
            if (ignoreLimitation) {
                sendRequest();
            } else {
                var confirmMsg = "prevac.visitReschedule.confirm.shouldSavePlannedDate";
                if ($scope.form.dto.actualDate !== ""
                    && $scope.form.dto.actualDate !== undefined
                    && $scope.form.dto.actualDate !== null) {
                    confirmMsg = "prevac.visitReschedule.confirm.shouldSaveActualDate";
                }
                motechConfirm(confirmMsg, "prevac.confirm",
                    function(confirmed) {
                        if (confirmed) {
                            var daysBetween = Math.round((new Date - $scope.parseDate($scope.form.dto.actualDate))/(1000*60*60*24));
                            if (daysBetween > 7) {
                                motechConfirm("prevac.visitReschedule.confirm.shouldSaveOldActualDate", "prevac.confirm",
                                    function(confirmed) {
                                    if (confirmed) {
                                        sendRequest();
                                    }
                                })
                            } else {
                                sendRequest();
                            }
                        }
                })
            }
        };

        $scope.formIsFilled = function() {
            return $scope.form
                && $scope.form.dto
                && ($scope.form.dto.actualDate || ($scope.form.dto.plannedDate && $scope.form.dto.startTime));
        };

        $scope.exportInstance = function() {
            var sortColumn, sortDirection, url = "../prevac/exportInstances/visitReschedule";
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithFilter === true) {
                url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                if ($scope.selectedFilter.startDate) {
                    url = url + "&startDate=" + $scope.selectedFilter.startDate;
                }

                if ($scope.selectedFilter.endDate) {
                    url = url + "&endDate=" + $scope.selectedFilter.endDate;
                }
            }

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#visitReschedule').getGridParam('sortname');
                sortDirection = $('#visitReschedule').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            $scope.exportInstanceWithUrl(url);
        };

        $scope.$watch('form.dto.ignoreDateLimitation', function (value) {
            if ($scope.form && $scope.form.dto) {
                if (!value) {
                    $scope.form.dto.minDate = $scope.earliestDateToReturn;
                    $scope.form.dto.maxDate = $scope.latestDateToReturn;
                } else {
                    var plannedDate = $scope.parseDate($scope.form.dto.plannedDate);
                    var currentDate = new Date();
                    currentDate.setHours(0, 0, 0, 0);
                    if (plannedDate < currentDate) {
                        $scope.form.dto.minDate = plannedDate;
                    } else {
                        $scope.form.dto.minDate = currentDate;
                    }
                    $scope.form.dto.maxDate = null;
                }
            }
        });

        $scope.setPrintData = function(document, location, participantId, participantName, plannedDate) {

            $('#versionDate', document).html($filter('date')(new Date(), $scope.cardDateTimeFormat));
            $('#location', document).html(location);
            $('#subjectId', document).html(participantId);
            $('#subjectName', document).html(participantName);
            $('#date', document).html($filter('date')($scope.parseDate(plannedDate), $scope.cardDateFormat));
        };

        $scope.print = function() {

            setTimeout(function() {
                var subjectId = $scope.visitForPrint.participantId;
                var date = $scope.visitForPrint.plannedDate;
                var subjectName = $scope.visitForPrint.participantName;
                var location = $scope.visitForPrint.location;

                var winPrint = window.open("../prevac/resources/partials/card/visitRescheduleCard.html");
                 if ((!(window.ActiveXObject) && "ActiveXObject" in window) || (navigator.userAgent.indexOf("MSIE") > -1)) {
                   // iexplorer
                    var windowOnload = winPrint.onload || function() {
                        setTimeout(function(){
                            $scope.setPrintData(winPrint.document, location, subjectId, subjectName, date);
                            winPrint.focus();
                            winPrint.print();
                        }, 500);
                      };

                      winPrint.onload = new function() { windowOnload(); } ;
                 } else {
                    winPrint.onload = function() {
                        $scope.setPrintData(winPrint.document, location, subjectId, subjectName, date);
                        winPrint.focus();
                        winPrint.print();
                    }
                 }
             }, 500);
        };
    });

    controllers.controller('PrevacCapacityInfoCtrl', function ($scope) {
        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];
    });

    controllers.controller('PrevacReportsCtrl', function ($scope, $http, $timeout) {
        $scope.getLookups("../prevac/getLookupsForCapacityReport");

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];

        $scope.exportInstance = function() {
            var sortColumn, sortDirection, url = "../prevac/exportInstances/capacityReports";
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;
            url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

            if ($scope.selectedFilter.startDate) {
                url = url + "&startDate=" + $scope.selectedFilter.startDate;
            }

            if ($scope.selectedFilter.endDate) {
                url = url + "&endDate=" + $scope.selectedFilter.endDate;
            }

            $scope.exportInstanceWithUrl(url);
        };
    });

}());
