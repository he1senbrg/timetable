package com.vte.timetable

import androidx.wear.protolayout.ActionBuilders.LoadAction
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DEVICE_PLATFORM_WEAR_OS
import androidx.wear.protolayout.ModifiersBuilders

val emptyClickable =
    ModifiersBuilders.Clickable.Builder().setOnClick(LoadAction.Builder().build()).setId("").build()

val emptyPar =
    DeviceParametersBuilders.DeviceParameters.Builder().setDevicePlatform(DEVICE_PLATFORM_WEAR_OS)
        .build()