import com.fasterxml.jackson.annotation.JsonView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/public/address")
public class AddressController {


    @JsonView(View.PUBLIC.class)
    @RequestMapping(method = RequestMethod.GET)
    public CompletableFuture<ResultObject> getAddresses(UserAuthentication authentication) {
        return CompletableFuture.supplyAsync(() -> {
            Customer customer = customerService.loadCustomer(getUserIdOrFail(authentication));

            return new ResultObject(customer.getAddresses());
        });
    }


}
