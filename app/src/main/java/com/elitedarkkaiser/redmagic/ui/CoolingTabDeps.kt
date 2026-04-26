package com.elitedarkkaiser.redmagic.ui

import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

data class CoolingTabDeps(
    val scrollTabContainer: () -> LinearLayout,
    val sectionPanel: () -> LinearLayout,
    val sectionHeader: (String, String) -> LinearLayout,
    val subtleLabel: (String) -> TextView,
    val bodyText: (String) -> TextView,
    val segmentedChip: (String, Boolean, () -> Unit) -> Button,
    val actionButton: (String, Boolean, () -> Unit) -> Button,
    val row: (Button, Button) -> LinearLayout,
    val singleRow: (Button) -> LinearLayout,
    val space: (Int) -> TextView,
    val spacer: (Int) -> TextView,
    val dp: (Int) -> Int,

    val setSelectedCurveSaved: (String) -> Unit,
    val setAutoFanEnabledSaved: (Boolean) -> Unit,
    val startAutoFanService: () -> Unit,
    val stopAutoFanService: () -> Unit,
    val startAutoPumpService: () -> Unit,
    val stopAutoPumpService: () -> Unit,
    val savePumpState: () -> Unit,
    val saveAutoPumpState: () -> Unit,
    val refreshStatus: () -> Unit,
    val refreshSmartPumpStatusViews: () -> Unit,
    val buildAutoPumpStatusText: () -> Pair<String, String>,
    val applyPumpProfile: (String) -> Unit,
    val confirmExperimentalPumpThenApply: () -> Unit,
    val setPumpManualControlsEnabled: (Boolean) -> Unit,
    val refreshAutoPumpUi: () -> Unit,
    val updateManualCurveUiState: () -> Unit,
    val applyFanLedPreviewIfEnabled: () -> Unit,
    val applyLogoLedPreviewIfEnabled: () -> Unit,
    val applyShoulderLedPreviewIfEnabled: () -> Unit
)
