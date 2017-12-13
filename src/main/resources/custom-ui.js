$scope.showBackToEntityListButton = false;
$scope.showAddInstanceButton = false;
$scope.showLookupButton = true;
$scope.showFieldsButton = false;
$scope.showImportButton = false;
$scope.showExportButton = false;
$scope.showViewTrashButton = false;
$scope.showFiltersButton = false;
$scope.showDeleteInstanceButton = false;

$scope.showAdvanced = false;
$scope.advancedButtonIndex = 6;

$scope.showOrHideAdvanced = function() {
    var i;
    $scope.showAdvanced = !$scope.showAdvanced;

    for (i = $scope.advancedButtonIndex + 1; i < $scope.fields.length; i += 1) {
        $scope.fields[i].nonDisplayable = !$scope.showAdvanced;
    }
};

$scope.getAdvancedButtonLabel = function() {
    if ($scope.showAdvanced) {
        return $scope.msg('prevac.hideAdvanced');
    } else {
        return $scope.msg('prevac.showAdvanced');
    }
};

$scope.editInstance = function(id, module, entityName) {
    blockUI();
    $scope.setHiddenFilters();
    $scope.instanceEditMode = false;
    $scope.setModuleEntity(module, entityName);
    $scope.loadedFields = Instances.selectInstance({
        id: $scope.selectedEntity.id,
        param: id
        },
        function (data) {
            $scope.selectedInstance = id;
            $scope.currentRecord = data;
            $scope.fields = data.fields;

            if (entityName === "Clinic") {
                $http.get('../prevac/prevac-config')
                .success(function(response) {
                    var i, j, fieldsMap = {},
                        clinicMainFields = response.clinicMainFields,
                        clinicExtendedFields = response.clinicExtendedFields,
                        showAdvancedButton = {
                            'name': 'showAdvanced',
                            'displayName': '',
                            'tooltip': '',
                            'value': '',
                            'type': {
                                'displayName': 'mds.field.string'
                            },
                            'required': false,
                            'nonEditable': false,
                            'nonDisplayable': false
                        };

                    $scope.showAdvanced = false;

                    for (i = 0; i < $scope.fields.length; i += 1) {
                        if ($scope.fields[i].name === "location" || $scope.fields[i].name === "siteId") {
                            $scope.fields[i].nonEditable = true;
                        }
                        fieldsMap[$scope.fields[i].displayName] = $scope.fields[i];
                    }

                    $scope.fields = [];

                    for (i = 0; i < clinicMainFields.length; i += 1) {
                        $scope.fields[i] = fieldsMap[clinicMainFields[i]];
                    }

                    $scope.advancedButtonIndex = i;
                    $scope.fields[i] = showAdvancedButton;

                    for (j = 0; j < clinicExtendedFields.length; j += 1) {
                        i += 1;
                        $scope.fields[i] = fieldsMap[clinicExtendedFields[j]];
                        $scope.fields[i].nonDisplayable = true;
                    }
                });
            }

            unblockUI();
        }, angularHandler('mds.error', 'mds.error.cannotUpdateInstance'));
};

$scope.addEntityInstanceDefault = $scope.addEntityInstance;

$scope.addEntityInstance = function () {
    $scope.fields.splice($scope.advancedButtonIndex, 1);
    $scope.addEntityInstanceDefault();
};

$scope.showLookupDialog = function() {
    $("#lookup-dialog")
    .css({'top': ($("#lookupDialogButton").offset().top - $("#main-content").offset().top) - 40,
    'left': ($("#lookupDialogButton").offset().left - $("#main-content").offset().left) - 15})
    .toggle();
    $("div.arrow").css({'left': 50});
};

$scope.loadEditValueFormDefault = $scope.loadEditValueForm;

$scope.loadEditValueForm = function (field) {
    if (field.name === 'showAdvanced') {
        return '../prevac/resources/partials/widgets/field-show-advanced.html';
    }

    return $scope.loadEditValueFormDefault(field);
};

$scope.msg = function () {
    if (arguments !== undefined && arguments !== null && arguments.length === 1) {
        if (arguments[0] === 'mds.btn.lookup') {
            arguments[0] = 'prevac.btn.lookup';
        } else if (arguments[0] === 'mds.form.label.lookup') {
            arguments[0] = 'prevac.form.label.lookup';
        }
    }
    return $scope.$parent.msg.apply(null, arguments);
};
