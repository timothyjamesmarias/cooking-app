package com.timothymarias.cookingapp.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "units")
class Unit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var symbol: String,

    @Column(name = "measurement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var measurementType: MeasurementType,

    @Column(name = "base_conversion_factor", nullable = false)
    var baseConversionFactor: Double,

    // For sync engine: store the frontend's local_id
    @Column(name = "local_id", unique = true)
    var localId: String? = null,

    @Column(nullable = false)
    var version: Int = 1,

    @Column(name = "last_modified", nullable = false)
    var lastModified: Instant = Instant.now(),

    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY)
    var quantities: MutableSet<Quantity> = mutableSetOf()
) {
    constructor() : this(null, "", "", MeasurementType.COUNT, 1.0, null, 1, Instant.now(), mutableSetOf())
}

enum class MeasurementType {
    WEIGHT,
    VOLUME,
    COUNT
}