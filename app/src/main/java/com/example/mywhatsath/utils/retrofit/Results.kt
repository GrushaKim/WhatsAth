package com.example.mywhatsath.utils.retrofit


class Results {

    var icon: String ?= null
    var geometry: Geometry ?= null
    var photos: Array<Photos> ?= null
    var id: String ?= null
    var name: String ?= null
    var place_id: String ?= null
    var price_level: Int ?= 0
    var rating: Double ?= 0.0
    var types: Array<String> ?= null
    var reference: String ?= null
    var scope: String ?= null
    var vicinity: String ?= null

}

class Geometry {
    var viewport: Viewport ?= null
    var location: Location ?= null
}

class Viewport{
    var northEast: NorthEast ?= null
    var southEast: SouthEast ?= null
}

class NorthEast{
    var lat: Double = 0.0
    var lng: Double = 0.0
}

class SouthEast{
    var lat: Double = 0.0
    var lng: Double = 0.0
}

class Location{
    var lat: Double = 0.0
    var lng: Double = 0.0
}