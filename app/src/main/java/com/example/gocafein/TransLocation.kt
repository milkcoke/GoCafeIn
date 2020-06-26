package com.example.gocafein


// 안드로이드 개발자 김한국씨 Katec <-> 경도/위도 좌표 변환 소스
// Reference URL : https://www.androidpub.com/1318647
class TransLocation {
    val GEO = 0
    val KATEC = 1
    val TM = 2
    val GRS80 = 3

    private val m_Ind = DoubleArray(3)
    private val m_Es = DoubleArray(3)
    private val m_Esp = DoubleArray(3)
    private val src_m = DoubleArray(3)
    private val dst_m = DoubleArray(3)

    private val EPSLN = 0.0000000001
    private val m_arMajor = DoubleArray(3)
    private val m_arMinor = DoubleArray(3)

    private val m_arScaleFactor = DoubleArray(3)
    private val m_arLonCenter = DoubleArray(3)
    private val m_arLatCenter = DoubleArray(3)
    private val m_arFalseNorthing = DoubleArray(3)
    private val m_arFalseEasting = DoubleArray(3)

    private val datum_params = DoubleArray(3)

    init {
        m_arScaleFactor[GEO] = 1.0
        m_arLonCenter[GEO] = 0.0
        m_arLatCenter[GEO] = 0.0
        m_arFalseNorthing[GEO] = 0.0
        m_arFalseEasting[GEO] = 0.0
        m_arMajor[GEO] = 6378137.0
        m_arMinor[GEO] = 6356752.3142

        m_arScaleFactor[KATEC] = 0.9999//0.9996;
        //m_arLonCenter[KATEC] = 2.22529479629277; // 127.5
        m_arLonCenter[KATEC] = 2.23402144255274 // 128
        m_arLatCenter[KATEC] = 0.663225115757845
        m_arFalseNorthing[KATEC] = 600000.0
        m_arFalseEasting[KATEC] = 400000.0
        m_arMajor[KATEC] = 6377397.155
        m_arMinor[KATEC] = 6356078.9633422494

        m_arScaleFactor[TM] = 1.0
        //this.m_arLonCenter[TM] = 2.21656815003280; // 127
        m_arLonCenter[TM] = 2.21661859489671 // 127.+10.485 minute
        m_arLatCenter[TM] = 0.663225115757845
        m_arFalseNorthing[TM] = 500000.0
        m_arFalseEasting[TM] = 200000.0
        m_arMajor[TM] = 6377397.155
        m_arMinor[TM] = 6356078.9633422494

        datum_params[0] = -146.43
        datum_params[1] = 507.89
        datum_params[2] = 681.46
        var tmp : Double = m_arMinor [GEO] / m_arMajor[GEO]
        m_Es[GEO] = 1.0 - tmp * tmp
        m_Esp[GEO] = m_Es[GEO] / (1.0 - m_Es[GEO])

        if (m_Es[GEO] < 0.00001) {
            m_Ind[GEO] = 1.0
        } else {
            m_Ind[GEO] = 0.0
        }

        tmp = m_arMinor[KATEC] / m_arMajor[KATEC]
        m_Es[KATEC] = 1.0 - tmp * tmp
        m_Esp[KATEC] = m_Es[KATEC] / (1.0 - m_Es[KATEC])

        if (m_Es[KATEC] < 0.00001) {
            m_Ind[KATEC] = 1.0
        } else {
            m_Ind[KATEC] = 0.0
        }

        tmp = m_arMinor[TM] / m_arMajor[TM]
        m_Es[TM] = 1.0 - tmp * tmp
        m_Esp[TM] = m_Es[TM] / (1.0 - m_Es[TM])

        if (m_Es[TM] < 0.00001) {
            m_Ind[TM] = 1.0
        } else {
            m_Ind[TM] = 0.0
        }

        src_m[GEO] = m_arMajor[GEO] * mlfn(
            e0fn(m_Es[GEO]),
            e1fn(m_Es[GEO]),
            e2fn(m_Es[GEO]),
            e3fn(m_Es[GEO]),
            m_arLatCenter[GEO]
        )
        dst_m[GEO] = m_arMajor[GEO] * mlfn(
            e0fn(m_Es[GEO]),
            e1fn(m_Es[GEO]),
            e2fn(m_Es[GEO]),
            e3fn(m_Es[GEO]),
            m_arLatCenter[GEO]
        )
        src_m[KATEC] = m_arMajor[KATEC] * mlfn(
            e0fn(m_Es[KATEC]),
            e1fn(m_Es[KATEC]),
            e2fn(m_Es[KATEC]),
            e3fn(m_Es[KATEC]),
            m_arLatCenter[KATEC]
        )
        dst_m[KATEC] = m_arMajor[KATEC] * mlfn(
            e0fn(m_Es[KATEC]),
            e1fn(m_Es[KATEC]),
            e2fn(m_Es[KATEC]),
            e3fn(m_Es[KATEC]),
            m_arLatCenter[KATEC]
        )
        src_m[TM] = m_arMajor[TM] * mlfn(
            e0fn(m_Es[TM]),
            e1fn(m_Es[TM]),
            e2fn(m_Es[TM]),
            e3fn(m_Es[TM]),
            m_arLatCenter[TM]
        )
        dst_m[TM] = m_arMajor[TM] * mlfn(
            e0fn(m_Es[TM]),
            e1fn(m_Es[TM]),
            e2fn(m_Es[TM]),
            e3fn(m_Es[TM]),
            m_arLatCenter[TM]
        )
    }


    private fun D2R(degree: Double): Double {
        return degree * Math.PI / 180.0
    }

    private fun R2D(radian: Double): Double {
        return radian * 180.0 / Math.PI
    }

    private fun e0fn(x: Double): Double {
        return 1.0 - 0.25 * x * (1.0 + x / 16.0 * (3.0 + 1.25 * x))
    }

    private fun e1fn(x: Double): Double {
        return 0.375 * x * (1.0 + 0.25 * x * (1.0 + 0.46875 * x))
    }

    private fun e2fn(x: Double): Double {
        return 0.05859375 * x * x * (1.0 + 0.75 * x)
    }

    private fun e3fn(x: Double): Double {
        return x * x * x * (35.0 / 3072.0)
    }

    private fun mlfn(
        e0: Double,
        e1: Double,
        e2: Double,
        e3: Double,
        phi: Double
    ): Double {
        return e0 * phi - e1 * Math.sin(2.0 * phi) + e2 * Math.sin(4.0 * phi) - e3 * Math.sin(
            6.0 * phi
        )
    }

    private fun asinz(value: Double): Double {
        var value = value
        if (Math.abs(value) > 1.0) value = (if (value > 0) 1 else -1).toDouble()
        return Math.asin(value)
    }

//   transLocation.convert(transLocation.KATEC, transLocation.GEO, naverKatechLocationPoint)
    fun convert(sourceType: Int, objectiveType: Int, inputType: GeoTransPoint): GeoTransPoint {
        val tempPoint = GeoTransPoint()
        val resultPoint = GeoTransPoint()
        if (sourceType == GEO) {
            tempPoint.x = D2R(inputType.x)
            tempPoint.y = D2R(inputType.y)
        } else {
            tm2geo(sourceType, inputType, tempPoint)
        }
        if (objectiveType == GEO) {
            resultPoint.x = R2D(tempPoint.x)
            resultPoint.y = R2D(tempPoint.y)
        } else {
            geo2tm(objectiveType, tempPoint, resultPoint)
            //objectiveType.x = Math.round(objectiveType.x);
            //objectiveType.y = Math.round(objectiveType.y);
        }
        return resultPoint
    }

    fun geo2tm(objectiveType: Int, inputPoint: GeoTransPoint, outputPoint: GeoTransPoint) {
        val x: Double
        val y: Double
        transform(GEO, objectiveType, inputPoint)
        val delta_lon: Double = inputPoint.x - m_arLonCenter[objectiveType]
        val sin_phi = Math.sin(inputPoint.y)
        val cos_phi = Math.cos(inputPoint.y)
        if (m_Ind[objectiveType] != 0.0) {
            val b = cos_phi * Math.sin(delta_lon)
            if (Math.abs(Math.abs(b) - 1.0) < EPSLN) {
                //Log.d("무한대 에러");
                //System.out.println("무한대 에러");
            }
        } else {
            val b = 0.0
            x =
                0.5 * m_arMajor[objectiveType] * m_arScaleFactor[objectiveType] * Math.log((1.0 + b) / (1.0 - b))
            var con = Math.acos(
                cos_phi * Math.cos(delta_lon) / Math.sqrt(1.0 - b * b)
            )
            if (inputPoint.y < 0) {
                con = con * -1
                y =
                    m_arMajor[objectiveType] * m_arScaleFactor[objectiveType] * (con - m_arLatCenter[objectiveType])
            }
        }
        val al = cos_phi * delta_lon
        val als = al * al
        val c = m_Esp[objectiveType] * cos_phi * cos_phi
        val tq = Math.tan(inputPoint.y)
        val t = tq * tq
        val con = 1.0 - m_Es[objectiveType] * sin_phi * sin_phi
        val n = m_arMajor[objectiveType] / Math.sqrt(con)
        val ml = m_arMajor[objectiveType] * mlfn(
            e0fn(m_Es[objectiveType]),
            e1fn(m_Es[objectiveType]),
            e2fn(m_Es[objectiveType]),
            e3fn(m_Es[objectiveType]),
            inputPoint.y
        )
        outputPoint.x =
            m_arScaleFactor[objectiveType] * n * al * (1.0 + als / 6.0 * (1.0 - t + c + als / 20.0 * (5.0 - 18.0 * t + t * t + 72.0 * c - 58.0 * m_Esp[objectiveType]))) + m_arFalseEasting[objectiveType]
        outputPoint.y =
            m_arScaleFactor[objectiveType] * (ml - dst_m[objectiveType] + n * tq * (als * (0.5 + als / 24.0 * (5.0 - t + 9.0 * c + 4.0 * c * c + als / 30.0 * (61.0 - 58.0 * t + t * t + 600.0 * c - 330.0 * m_Esp[objectiveType]))))) + m_arFalseNorthing[objectiveType]
    }


    fun tm2geo(sourceType: Int, inputPoint: GeoTransPoint, outputPoint: GeoTransPoint) {
        val tmpPt = GeoTransPoint(inputPoint.x, inputPoint.y)
        val max_iter = 6
        if (m_Ind[sourceType] != 0.0) {
            val f =
                Math.exp(inputPoint.x / (m_arMajor[sourceType] * m_arScaleFactor[sourceType]))
            val g = 0.5 * (f - 1.0 / f)
            val temp: Double =
                m_arLatCenter[sourceType] + tmpPt.y / (m_arMajor[sourceType] * m_arScaleFactor[sourceType])
            val h = Math.cos(temp)
            val con = Math.sqrt((1.0 - h * h) / (1.0 + g * g))
            outputPoint.y = asinz(con)
            if (temp < 0) outputPoint.y *= -1
            if (g == 0.0 && h == 0.0) {
                outputPoint.x = m_arLonCenter[sourceType]
            } else {
                outputPoint.x = Math.atan(g / h) + m_arLonCenter[sourceType]
            }
        }
        tmpPt.x -= m_arFalseEasting[sourceType]
        tmpPt.y -= m_arFalseNorthing[sourceType]
        val con: Double =
            (src_m[sourceType] + tmpPt.y / m_arScaleFactor[sourceType]) / m_arMajor[sourceType]
        var phi = con
        var i = 0
        while (true) {
            val delta_Phi =
                (con + e1fn(m_Es[sourceType]) * Math.sin(2.0 * phi) - e2fn(
                    m_Es[sourceType]
                ) * Math.sin(4.0 * phi) + e3fn(m_Es[sourceType]) * Math.sin(6.0 * phi)) / e0fn(
                    m_Es[sourceType]
                ) - phi
            phi = phi + delta_Phi
            if (Math.abs(delta_Phi) <= EPSLN) break
            if (i >= max_iter) {
                //Log.d("무한대 에러");
                //System.out.println("무한대 에러");
                break
            }
            i++
        }
        if (Math.abs(phi) < Math.PI / 2) {
            val sin_phi = Math.sin(phi)
            val cos_phi = Math.cos(phi)
            val tan_phi = Math.tan(phi)
            val c = m_Esp[sourceType] * cos_phi * cos_phi
            val cs = c * c
            val t = tan_phi * tan_phi
            val ts = t * t
            val cont = 1.0 - m_Es[sourceType] * sin_phi * sin_phi
            val n = m_arMajor[sourceType] / Math.sqrt(cont)
            val r = n * (1.0 - m_Es[sourceType]) / cont
            val d: Double = tmpPt.x / (n * m_arScaleFactor[sourceType])
            val ds = d * d
            outputPoint.y =
                phi - n * tan_phi * ds / r * (0.5 - ds / 24.0 * (5.0 + 3.0 * t + 10.0 * c - 4.0 * cs - 9.0 * m_Esp[sourceType] - ds / 30.0 * (61.0 + 90.0 * t + 298.0 * c + 45.0 * ts - 252.0 * m_Esp[sourceType] - 3.0 * cs)))
            outputPoint.x =
                m_arLonCenter[sourceType] + d * (1.0 - ds / 6.0 * (1.0 + 2.0 * t + c - ds / 20.0 * (5.0 - 2.0 * c + 28.0 * t - 3.0 * cs + 8.0 * m_Esp[sourceType] + 24.0 * ts))) / cos_phi
        } else {
            outputPoint.y = Math.PI * 0.5 * Math.sin(tmpPt.y)
            outputPoint.x = m_arLonCenter[sourceType]
        }
        transform(sourceType, GEO, outputPoint)
    }

    fun getDistancebyGeo(pt1: GeoTransPoint, pt2: GeoTransPoint): Double {
        val lat1 = D2R(pt1.y)
        val lon1 = D2R(pt1.x)
        val lat2 = D2R(pt2.y)
        val lon2 = D2R(pt2.x)
        val longitude = lon2 - lon1
        val latitude = lat2 - lat1
        val a = Math.pow(
            Math.sin(latitude / 2.0),
            2.0
        ) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(
            Math.sin(
                longitude / 2.0
            ), 2.0
        )
        return 6376.5 * 2.0 * Math.atan2(
            Math.sqrt(a),
            Math.sqrt(1.0 - a)
        )
    }

    fun getDistancebyKatec(pt1: GeoTransPoint, pt2: GeoTransPoint): Double {
        var pt1: GeoTransPoint = pt1
        var pt2: GeoTransPoint = pt2
        pt1 = convert(KATEC, GEO, pt1)
        pt2 = convert(KATEC, GEO, pt2)
        return getDistancebyGeo(pt1, pt2)
    }

    fun getDistancebyTm(pt1: GeoTransPoint, pt2: GeoTransPoint): Double {
        var pt1: GeoTransPoint = pt1
        var pt2: GeoTransPoint = pt2
        pt1 = convert(TM, GEO, pt1)
        pt2 = convert(TM, GEO, pt2)
        return getDistancebyGeo(pt1, pt2)
    }

    private fun getTimebySec(distance: Double): Long {
        return Math.round(3600 * distance / 4)
    }

    fun getTimebyMin(distance: Double): Long {
        return Math.ceil(getTimebySec(distance) / 60.toDouble()).toLong()
    }

    /*
	Author:       Richard Greenwood rich@greenwoodmap.com
	License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html
	*/

    /*
	Author:       Richard Greenwood rich@greenwoodmap.com
	License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html
	*/
    /**
     * convert between geodetic coordinates (longitude, latitude, height)
     * and gecentric coordinates (X, Y, Z)
     * ported from Proj 4.9.9 geocent.c
     */
    // following constants from geocent.c
    private val HALF_PI = 0.5 * Math.PI
    private val COS_67P5 = 0.38268343236508977 /* cosine of 67.5 degrees */
    private val AD_C = 1.0026000
    /* Toms region 1 constant */

    /* Toms region 1 constant */
    private fun transform(srctype: Int, dsttype: Int, point: GeoTransPoint) {
        if (srctype == dsttype) return
        if (srctype != 0 || dsttype != 0) {
            // Convert to geocentric coordinates.
            geodeticToGeocentric(srctype, point)

            // Convert between datums
            if (srctype != 0) {
                geocentricToWgs84(point)
            }
            if (dsttype != 0) {
                geocentricFromWgs84(point)
            }

            // Convert back to geodetic coordinates
            geocentricToGeodetic(dsttype, point)
        }
    }

    private fun geodeticToGeocentric(type: Int, p: GeoTransPoint): Boolean {

        /*
	 * The function Convert_Geodetic_To_Geocentric converts geodetic coordinates
	 * (latitude, longitude, and height) to geocentric coordinates (X, Y, Z),
	 * according to the current ellipsoid parameters.
	 *
	 *    Latitude  : Geodetic latitude in radians                     (input)
	 *    Longitude : Geodetic longitude in radians                    (input)
	 *    Height    : Geodetic height, in meters                       (input)
	 *    X         : Calculated Geocentric X coordinate, in meters    (output)
	 *    Y         : Calculated Geocentric Y coordinate, in meters    (output)
	 *    Z         : Calculated Geocentric Z coordinate, in meters    (output)
	 *
	 */
        var Longitude: Double = p.x
        var Latitude: Double = p.y
        val Height: Double = p.z
        val X: Double // output
        val Y: Double
        val Z: Double
        val Rn: Double /*  Earth radius at location  */
        val Sin_Lat: Double /*  Math.sin(Latitude)  */
        val Sin2_Lat: Double /*  Square of Math.sin(Latitude)  */
        val Cos_Lat: Double /*  Math.cos(Latitude)  */

        /*
	  ** Don't blow up if Latitude is just a little out of the value
	  ** range as it may just be a rounding issue.  Also removed longitude
	  ** test, it should be wrapped by Math.cos() and Math.sin().  NFW for PROJ.4, Sep/2001.
	  */if (Latitude < -HALF_PI && Latitude > -1.001 * HALF_PI) Latitude =
            -HALF_PI else if (Latitude > HALF_PI && Latitude < 1.001 * HALF_PI) Latitude =
            HALF_PI else if (Latitude < -HALF_PI || Latitude > HALF_PI) { /* Latitude out of range */
            return true
        }

        /* no errors */if (Longitude > Math.PI) Longitude -= 2 * Math.PI
        Sin_Lat = Math.sin(Latitude)
        Cos_Lat = Math.cos(Latitude)
        Sin2_Lat = Sin_Lat * Sin_Lat
        Rn = m_arMajor[type] / Math.sqrt(1.0e0 - m_Es[type] * Sin2_Lat)
        X = (Rn + Height) * Cos_Lat * Math.cos(Longitude)
        Y = (Rn + Height) * Cos_Lat * Math.sin(Longitude)
        Z = (Rn * (1 - m_Es[type]) + Height) * Sin_Lat
        p.x = X
        p.y = Y
        p.z = Z
        return false
    } // cs_geodetic_to_geocentric()


    /** Convert_Geocentric_To_Geodetic
     * The method used here is derived from 'An Improved Algorithm for
     * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
     */
    private fun geocentricToGeodetic(type: Int, p: GeoTransPoint) {
        val X: Double = p.x
        val Y: Double = p.y
        val Z: Double = p.z
        val Longitude: Double
        var Latitude = 0.0
        val Height: Double
        val W: Double /* distance from Z axis */
        val W2: Double /* square of distance from Z axis */
        val T0: Double /* initial estimate of vertical component */
        val T1: Double /* corrected estimate of vertical component */
        val S0: Double /* initial estimate of horizontal component */
        val S1: Double /* corrected estimate of horizontal component */
        val Sin_B0: Double /* Math.sin(B0), B0 is estimate of Bowring aux doubleiable */
        val Sin3_B0: Double /* cube of Math.sin(B0) */
        val Cos_B0: Double /* Math.cos(B0) */
        val Sin_p1: Double /* Math.sin(phi1), phi1 is estimated latitude */
        val Cos_p1: Double /* Math.cos(phi1) */
        val Rn: Double /* Earth radius at location */
        val Sum: Double /* numerator of Math.cos(phi1) */
        var At_Pole: Boolean /* indicates location is in polar region */
        At_Pole = false
        if (X != 0.0) {
            Longitude = Math.atan2(Y, X)
        } else {
            if (Y > 0) {
                Longitude = HALF_PI
            } else if (Y < 0) {
                Longitude = -HALF_PI
            } else {
                At_Pole = true
                Longitude = 0.0
                if (Z > 0.0) {  /* north pole */
                    Latitude = HALF_PI
                } else if (Z < 0.0) {  /* south pole */
                    Latitude = -HALF_PI
                } else {  /* center of earth */
                    Latitude = HALF_PI
                    Height = -m_arMinor[type]
                    return
                }
            }
        }
        W2 = X * X + Y * Y
        W = Math.sqrt(W2)
        T0 = Z * AD_C
        S0 = Math.sqrt(T0 * T0 + W2)
        Sin_B0 = T0 / S0
        Cos_B0 = W / S0
        Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0
        T1 = Z + m_arMinor[type] * m_Esp[type] * Sin3_B0
        Sum = W - m_arMajor[type] * m_Es[type] * Cos_B0 * Cos_B0 * Cos_B0
        S1 = Math.sqrt(T1 * T1 + Sum * Sum)
        Sin_p1 = T1 / S1
        Cos_p1 = Sum / S1
        Rn = m_arMajor[type] / Math.sqrt(1.0 - m_Es[type] * Sin_p1 * Sin_p1)
        Height = if (Cos_p1 >= COS_67P5) {
            W / Cos_p1 - Rn
        } else if (Cos_p1 <= -COS_67P5) {
            W / -Cos_p1 - Rn
        } else {
            Z / Sin_p1 + Rn * (m_Es[type] - 1.0)
        }
        if (At_Pole == false) {
            Latitude = Math.atan(Sin_p1 / Cos_p1)
        }
        p.x = Longitude
        p.y = Latitude
        p.z = Height
        return
    } // geocentric_to_geodetic()


    /** */ // geocentic_to_wgs84(defn, p )
    //  defn = coordinate system definition,
    //  p = point to transform in geocentric coordinates (x,y,z)
    private fun geocentricToWgs84(p: GeoTransPoint) {

        //if( defn.datum_type == PJD_3PARAM )
        run {

            // if( x[io] == HUGE_VAL )
            //    continue;
            p.x += datum_params[0]
            p.y += datum_params[1]
            p.z += datum_params[2]
        }
    } // geocentric_to_wgs84


    /** */ // geocentic_from_wgs84()
    //  coordinate system definition,
    //  point to transform in geocentric coordinates (x,y,z)
    private fun geocentricFromWgs84(p: GeoTransPoint) {

        //if( defn.datum_type == PJD_3PARAM )
        run {

            //if( x[io] == HUGE_VAL )
            //    continue;
            p.x -= datum_params[0]
            p.y -= datum_params[1]
            p.z -= datum_params[2]
        }
    } //geocentric_from_wgs84()

}