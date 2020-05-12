
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * api path: /api/public/address
 * method: POST
 */
@RunWith(SpringRunner.class)
@RestDocConfig
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class AddressCtrlV3AddAddressTest {

    @After
    public void tearDown() throws Exception {
        nothingElseMatters();
    }

    @Test
    public void success() throws Exception {
        Customer customer = CustomerTestHelper.createRandomValidCustomer();
        customer.setAddresses(new ArrayList<>());
        customer.getAddresses().add(AddressTestHelper.createRandomValidAddress());
        customer.getAddresses().add(AddressTestHelper.createRandomValidAddress());
        customer.getAddresses().add(AddressTestHelper.createRandomValidAddress());
        customer.setPrimaryAddress(customer.getAddresses().get(0));

        Address address = AddressTestHelper.createRandomValidAddress();

        //mock security
        given(tokenAuthenticationService.getAuthentication(any())).willReturn(mockAuth(customer, UserRole.CUSTOMER));

        given(customerService.addAddress(eq(customer.getId()), any(Address.class))).willReturn(address);

        MvcResult mvcResult = mockMvc.perform(post(API_PATH)
                .content(objectMapper.writerWithView(View.CUSTOMER.REQUEST_BODY.ADDRESS.class).writeValueAsString(address))
                .param("primary", "true")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(request().asyncStarted())

                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("message").value("آدرس جدید اضافه شد"))
                .andExpect(jsonPath("data").value(address.getId()))
                .andDo(document(DOC_PATH,
                        preprocessRequest(RestDocUtils.removeUnNecessaryRequestHeaders()),
                        preprocessResponse(RestDocUtils.removeUnNecessaryResponseHeaders()),
                        requestParameters(
                                parameterWithName("primary").description("should this address be changed to primary address or not")
                                        .optional()
                                        .attributes(key("extra").value("must be true or false, if not specified it will be considered `false`"))
                        ),
                        requestFields(

                                fieldWithPath("province")
                                        .description("province name")
                                        .type(String.class.getSimpleName())
                                        .attributes(key("extra").value(RegExUtil.TEXT_FA_SMALL_DESC)),
                                fieldWithPath("city")
                                        .description("city name")
                                        .type(String.class.getSimpleName())
                                        .attributes(key("extra").value(RegExUtil.TEXT_FA_SMALL_DESC)),
                                fieldWithPath("address")
                                        .description("complete text address")
                                        .type(String.class.getSimpleName())
                                        .attributes(key("extra").value(RegExUtil.TEXT_FA_LARGE_DESC))
                        ).and(LatLngTestHelper.docLink("location")),
                        responseFields(
                                fieldWithPath("message").description("result message")
                                        .type(String.class.getSimpleName()).attributes(key("extra").value("")),
                                fieldWithPath("data").description("new addresses id")
                                        .type(String.class.getSimpleName()).attributes(key("extra").value(""))
                        )
                ));

        verify(tokenAuthenticationService, times(2)).getAuthentication(any());
        verify(customerService, times(1)).addAddress(eq(customer.getId()), any(Address.class));
        verify(customerService, times(1)).confirmAddress(eq(customer.getId()), eq(address.getId()));
        verify(customerService, times(1)).changePrimaryAddress(eq(customer.getId()), eq(address.getId()));
    }

    @Test
    public void notPrimary() throws Exception {
        Customer customer = CustomerTestHelper.createRandomValidCustomer();
        Address address = AddressTestHelper.createRandomValidAddress();

        //mock security
        given(tokenAuthenticationService.getAuthentication(any())).willReturn(mockAuth(customer, UserRole.CUSTOMER));

        given(customerService.addAddress(eq(customer.getId()), any(Address.class))).willReturn(address);

        MvcResult mvcResult = mockMvc.perform(post(API_PATH)
                .content(objectMapper.writeValueAsString(address))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(request().asyncStarted())

                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("آدرس جدید اضافه شد"))
                .andExpect(jsonPath("data").value(address.getId()));

        verify(tokenAuthenticationService, times(2)).getAuthentication(any());
        verify(customerService, times(1)).addAddress(eq(customer.getId()), any(Address.class));
        verify(customerService, times(1)).confirmAddress(eq(customer.getId()), eq(address.getId()));
    }

    @Test
    public void updateError() throws Exception {
        Customer customer = CustomerTestHelper.createRandomValidCustomer();
        Address address = AddressTestHelper.createRandomValidAddress();

        //mock security
        given(tokenAuthenticationService.getAuthentication(any())).willReturn(mockAuth(customer, UserRole.CUSTOMER));

        given(customerService.addAddress(eq(customer.getId()), any(Address.class))).willReturn(null);

        MvcResult mvcResult = mockMvc.perform(post(API_PATH)
                .content(objectMapper.writeValueAsString(address))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(request().asyncStarted())

                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("خطای سرویس"))
                .andExpect(jsonPath("data").isEmpty());

        verify(tokenAuthenticationService, times(2)).getAuthentication(any());

        verify(customerService, times(1)).addAddress(eq(customer.getId()), any(Address.class));
    }

    @Test
    public void badRequest() throws Exception {
        Customer customer = CustomerTestHelper.createRandomValidCustomer();

        //mock security
        given(tokenAuthenticationService.getAuthentication(any())).willReturn(mockAuth(customer, UserRole.CUSTOMER));

        given(customerService.getCustomer(eq(customer.getId()))).willReturn(customer);

        //no request body at all
        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());

        //bad request bodies
        Map<Address, String> badRequests = new LinkedHashMap<>();
        badRequests.put(AddressTestHelper.createRandomValidAddress().setProvince(null), "استان الزامی است");
        badRequests.put(AddressTestHelper.createRandomValidAddress().setProvince("a"), "استان باید حاوی حروف، اعداد فارسی و علائم نگارشی بوده و با حرف شروع شود و طول آن حداکثر ۵۰ کاراکتر باشد");
        badRequests.put(AddressTestHelper.createRandomValidAddress().setCity(null), "شهر الزامی است");
        badRequests.put(AddressTestHelper.createRandomValidAddress().setCity("a"), "شهر باید حاوی حروف، اعداد فارسی و علائم نگارشی بوده و با حرف شروع شود و طول آن حداکثر ۵۰ کاراکتر باشد");
        badRequests.put(AddressTestHelper.createRandomValidAddress().setAddress(null), "آدرس الزامی است");
        badRequests.put(AddressTestHelper.createRandomValidAddress().setAddress("a"), "آدرس باید حاوی حروف و اعداد فارسی و علائم نگارشی بوده و با حرف شروع شود و طول آن حداکثر ۲۰۰ کاراکتر باشد");
        badRequests.put(AddressTestHelper.createRandomValidAddress().setLocation(null), "موقعیت جغرافیایی الزامی است");

        for (Address body : badRequests.keySet()) {
            mockMvc.perform(post(API_PATH)
                    .content(objectMapper.writeValueAsString(body))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("message").value(badRequests.get(body)))
                    .andExpect(jsonPath("data").isEmpty());
        }

        verify(tokenAuthenticationService, times(badRequests.size() + 1)).getAuthentication(any());
    }

    @Test
    public void forbidden() throws Exception {
        Customer customer = CustomerTestHelper.createRandomValidCustomer();

        for (UserRole userRole : UserRole.values()) {
            if (userRole != UserRole.CUSTOMER && userRole != UserRole.ADMIN) {
                //mock security
                given(tokenAuthenticationService.getAuthentication(any())).willReturn(mockAuth(customer, userRole));

                mockMvc.perform(post(API_PATH)).andExpect(status().isForbidden());
            }
        }

        verify(tokenAuthenticationService, times(UserRole.values().length - 2)).getAuthentication(any());
    }
}
