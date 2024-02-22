package com.nestapp.files

import com.nestapp.nest.data.Placement
import com.nestapp.project.parts.DxfPart

data class DxfPartPlacement(
    val placement: Placement,
    val part: DxfPart,
)
