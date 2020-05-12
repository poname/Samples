
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GeoServiceGuessLocationTest extends GeoServiceTestParent {

    @Before
    public void setUp() throws Exception {
        setupMocks();
    }

    @After
    public void tearDown() throws Exception {
        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotNullAndRegionNotNullAndDistrictNotNull() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = UUID.randomUUID().toString();
        String district = UUID.randomUUID().toString();

        District d = DistrictTestHelper.createRandomValidDistrict();
        given(districtRepository.findByProvinceAndCityAndRegionAndName(eq(province), eq(city), eq(region), eq(district))).willReturn(d);

        assertThat(geoService.guessLocation(province, city, region, district)).isEqualToComparingFieldByField(d.getCenter());

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(districtRepository, times(1)).findByProvinceAndCityAndRegionAndName(eq(province), eq(city), eq(region), eq(district));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotNullAndRegionNotNullAndDistrictWithoutCenter() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = UUID.randomUUID().toString();
        String district = UUID.randomUUID().toString();

        //district without center
        District d = DistrictTestHelper.createRandomValidDistrict().setCenter(null);
        given(districtRepository.findByProvinceAndCityAndRegionAndName(eq(province), eq(city), eq(region), eq(district))).willReturn(d);
        //region
        Region r = RegionTestHelper.createRandomValidRegion();
        given(regionRepository.findByProvinceAndCityAndName(eq(province), eq(city), eq(region))).willReturn(r);

        //must return center of region
        assertThat(geoService.guessLocation(province, city, region, district)).isEqualToComparingFieldByField(r.getCenter());

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(districtRepository, times(1)).findByProvinceAndCityAndRegionAndName(eq(province), eq(city), eq(region), eq(district));
        verify(regionRepository, times(1)).findByProvinceAndCityAndName(eq(province), eq(city), eq(region));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotNullAndRegionNotNullAndDistrictNotFound() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = UUID.randomUUID().toString();
        String district = UUID.randomUUID().toString();

        //district not found
        given(districtRepository.findByProvinceAndCityAndRegionAndName(eq(province), eq(city), eq(region), eq(district))).willReturn(null);
        //region
        Region r = RegionTestHelper.createRandomValidRegion();
        given(regionRepository.findByProvinceAndCityAndName(eq(province), eq(city), eq(region))).willReturn(r);

        //must return center of region
        assertThat(geoService.guessLocation(province, city, region, district)).isEqualTo(r.getCenter());

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(districtRepository, times(1)).findByProvinceAndCityAndRegionAndName(eq(province), eq(city), eq(region), eq(district));
        verify(regionRepository, times(1)).findByProvinceAndCityAndName(eq(province), eq(city), eq(region));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotNullAndRegionNotNullAndDistrictNull() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = UUID.randomUUID().toString();
        String district = null;

        //region
        Region r = RegionTestHelper.createRandomValidRegion();
        given(regionRepository.findByProvinceAndCityAndName(eq(province), eq(city), eq(region))).willReturn(r);

        assertThat(geoService.guessLocation(province, city, region, district)).isEqualToComparingFieldByField(r.getCenter());

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(regionRepository, times(1)).findByProvinceAndCityAndName(eq(province), eq(city), eq(region));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotNullAndRegionWithoutCenterAndDistrictNull() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = UUID.randomUUID().toString();
        String district = null;

        //region without center
        Region r = RegionTestHelper.createRandomValidRegion().setCenter(null);
        given(regionRepository.findByProvinceAndCityAndName(eq(province), eq(city), eq(region))).willReturn(r);
        //city
        City c = CityTestHelper.createRandomValidCity();
        given(cityRepository.findByProvinceAndCity(eq(province), eq(city))).willReturn(c);

        //must return center of city
        assertThat(geoService.guessLocation(province, city, region, district)).isEqualToComparingFieldByField(c.getCenter());

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(regionRepository, times(1)).findByProvinceAndCityAndName(eq(province), eq(city), eq(region));
        verify(cityRepository, times(1)).findByProvinceAndCity(eq(province), eq(city));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotNullAndRegionNotFoundAndDistrictNull() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = UUID.randomUUID().toString();
        String district = null;

        //region not found
        given(regionRepository.findByProvinceAndCityAndName(eq(province), eq(city), eq(region))).willReturn(null);
        //city
        City c = CityTestHelper.createRandomValidCity();
        given(cityRepository.findByProvinceAndCity(eq(province), eq(city))).willReturn(c);

        //must return center of city
        assertThat(geoService.guessLocation(province, city, region, district)).isEqualToComparingFieldByField(c.getCenter());

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(regionRepository, times(1)).findByProvinceAndCityAndName(eq(province), eq(city), eq(region));
        verify(cityRepository, times(1)).findByProvinceAndCity(eq(province), eq(city));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotNullAndRegionNullAndDistrictNull() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = null;
        String district = null;

        //city
        City c = CityTestHelper.createRandomValidCity();
        given(cityRepository.findByProvinceAndCity(eq(province), eq(city))).willReturn(c);

        assertThat(geoService.guessLocation(province, city, region, district)).isEqualTo(c.getCenter());

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(cityRepository, times(1)).findByProvinceAndCity(eq(province), eq(city));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityWithoutCenterAndRegionNullCenterAndDistrictNull() throws Exception {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = null;
        String district = null;

        //city without center
        City c = CityTestHelper.createRandomValidCity().setCenter(null);
        given(cityRepository.findByProvinceAndCity(eq(province), eq(city))).willReturn(c);

        LatLng tehranCenter = new LatLng(35.6922882927872D, 51.3862157380208D);

        //must return center of tehran
        assertThat(geoService.guessLocation(province, city, region, district)).isEqualToComparingFieldByField(tehranCenter);

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(cityRepository, times(1)).findByProvinceAndCity(eq(province), eq(city));

        nothingElseMatters();
    }

    @Test
    public void provinceNotNullAndCityNotFoundAndRegionNullAndDistrictNull() {
        String province = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String region = null;
        String district = null;

        //city not found
        given(cityRepository.findByProvinceAndCity(eq(province), eq(city))).willReturn(null);

        LatLng tehranCenter = new LatLng(35.6922882927872D, 51.3862157380208D);

        //must return center of tehran
        assertThat(geoService.guessLocation(province, city, region, district)).isEqualToComparingFieldByField(tehranCenter);

        verify(geoService, times(1)).guessLocation(eq(province), eq(city), eq(region), eq(district));

        verify(cityRepository, times(1)).findByProvinceAndCity(eq(province), eq(city));

        nothingElseMatters();
    }
}
