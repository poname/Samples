
public class GeoService {

    public LatLng guessLocation(String province, String city, String region, String district) {
        if (province != null) {
            if (city != null) {
                if (region != null) {
                    if (district != null) {
                        District d = districtRepository.findByProvinceAndCityAndRegionAndName(province, city, region, district);
                        if (d != null) {
                            if (d.getCenter() != null) {
                                return d.getCenter();
                            } else if (logger.isWarnEnabled()) {
                                logger.warn("guessLocation, district {} doesn't have center", d.getId());
                            }
                        }
                    }
                    //at this point district wasn't found -> find center of region
                    Region r = regionRepository.findByProvinceAndCityAndName(province, city, region);
                    if (r != null) {
                        if (r.getCenter() != null) {
                            return r.getCenter();
                        } else if (logger.isWarnEnabled()) {
                            logger.warn("guessLocation, region {} doesn't have center", r.getId());
                        }
                    }
                }
                //at this point region wasn't found -> find center of city
                City c = cityRepository.findByProvinceAndCity(province, city);
                if (c != null) {
                    if (c.getCenter() != null) {
                        return c.getCenter();
                    } else if (logger.isWarnEnabled()) {
                        logger.warn("guessLocation, city {} doesn't have center", c.getId());
                    }
                }
            }
        }
        //at this point just return center of tehran
        return new LatLng(35.6922882927872D, 51.3862157380208D);
    }

}