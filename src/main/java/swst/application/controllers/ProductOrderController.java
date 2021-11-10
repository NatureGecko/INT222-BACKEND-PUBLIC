package swst.application.controllers;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import swst.application.authenSecurity.TokenUtills;
import swst.application.entities.OrderDetail;
import swst.application.entities.OrderStatus;
import swst.application.entities.Orders;
import swst.application.entities.Products;
import swst.application.entities.ProductsColor;
import swst.application.entities.UsernamesModels;
import swst.application.errorsHandlers.ExceptionFoundation;
import swst.application.errorsHandlers.ExceptionresponsesModel.EXCEPTION_CODES;
import swst.application.models.ActionResponseModel;
import swst.application.repositories.OrderDetailRepository;
import swst.application.repositories.OrderStatusRepository;
import swst.application.repositories.OrdersRepository;
import swst.application.repositories.ProductsColorRepository;
import swst.application.repositories.ProductsRepository;
import swst.application.repositories.RolesRepository;
import swst.application.repositories.UsernameRepository;

@Service
@PropertySource("userdefined.properties")
@Slf4j
public class ProductOrderController {
	@Autowired
	private OrdersRepository ordersRepository;
	@Autowired
	private OrderDetailRepository orderDetailRepository;
	@Autowired
	private UsernameRepository usernameRepository;
	@Autowired
	private ProductsRepository productsRepository;
	@Autowired
	private ProductsColorRepository productsColorRepository;
	@Autowired
	private OrderStatusRepository orderStatusRepository;
	@Autowired
	private RolesRepository rolesRepository;

	@Value("${application.pagerequest.maxsize.orders}")
	private int maxsizeOrders;

	@Value("${application.pagerequest.defaultsize.orders}")
	private int defaultSizeOrders;

	// [ ListOrderByUserID]
	public Page<Orders> listOrderByUserID(int page, int size, HttpServletRequest request) {
		if (page < 0) {
			page = 0;
		}
		if (size < 1 || size > defaultSizeOrders) {
			size = defaultSizeOrders;
		}

		int currentUser = usernameRepository.findByUserName(TokenUtills.getUserNameFromToken(request)).getUserNameID();

		Pageable sendPageRequest = PageRequest.of(page, size);
		Page<Orders> result = ordersRepository.findByUserNameID(currentUser, sendPageRequest);

		if (result.getTotalPages() < page + 1) {
			throw new ExceptionFoundation(EXCEPTION_CODES.SEARCH_NOT_FOUND, "[ NOT FOUND ] Nothing here. :(");
		}

		return result;
	}

	// [ addOrder ]
	public ActionResponseModel addOrder(HttpServletRequest request, Orders orders) {

		int userNameId = usernameRepository.findByUserName(TokenUtills.getUserNameFromToken(request)).getUserNameID();
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		Orders addOrder = new Orders();

		addOrder.setUserNameID(userNameId);
		addOrder.setDateTime(currentTime.toString());
		addOrder.setAllPrice(0);
		addOrder.setPaymentDate(null);
		addOrder.setOrderStatus(orderStatusRepository.findByStatus("To Pay"));

		log.info("" + addOrder.getDateTime());

		addOrder = ordersRepository.save(addOrder);

		loopSaveOrderDetails(orders.getOrderDetails(), addOrder);
		addOrder.setOrderDetails(orders.getOrderDetails());

		return new ActionResponseModel("Adding orders.", true);
	}

	// [ loopSaveOrderDetails ]
	private Orders loopSaveOrderDetails(List<OrderDetail> orderDetailList, Orders addOrder) {

		int detailsNumber = orderDetailList.size();

		long[] productColorIdList = new long[detailsNumber];
		int[] orderQuantityList = new int[detailsNumber];

		float allPrice = 0.0f;

		for (int i = 0; i < detailsNumber; i++) {
			OrderDetail currentOrder = orderDetailList.get(i);
			OrderDetail newOrder = new OrderDetail();
			Optional<ProductsColor> targerProductColor = productsColorRepository
					.findById(currentOrder.getProductcolorID());
			if (targerProductColor == null) {
				deleteOrder(addOrder.getOrderID());
				throw new ExceptionFoundation(EXCEPTION_CODES.SHOP_NOT_ON_STORE,
						"[ NOT ON STORE ] This item is not exist or not for sell.");
			}
			Optional<Products> targerProduct = productsRepository.findById(targerProductColor.get().getCaseID());
			if (targerProduct == null) {
				deleteOrder(addOrder.getOrderID());
				throw new ExceptionFoundation(EXCEPTION_CODES.SHOP_NOT_ON_STORE,
						"[ NOT ON STORE ] This item is not exist or not for sell.");
			}
			newOrder.setQuantityOrder(currentOrder.getQuantityOrder());
			newOrder.setUnitPrice(targerProduct.get().getCasePrice());
			newOrder.setOrders(addOrder);
			newOrder.setProductcolorID(currentOrder.getProductcolorID());

			allPrice += (newOrder.getQuantityOrder() * newOrder.getUnitPrice());
			orderQuantityList[i] = newOrder.getQuantityOrder();
			productColorIdList[i] = newOrder.getProductcolorID();

			orderDetailRepository.save(newOrder);

		}

		for (int i = 0; i < productColorIdList.length; i++) {
			ProductsColor currentProduct = productsColorRepository.findById(productColorIdList[i])
					.orElseThrow(() -> new ExceptionFoundation(EXCEPTION_CODES.SEARCH_NOT_FOUND,
							"[ NOT FOUND ] Product of this type is not exist."));
			currentProduct.setQuantity(currentProduct.getQuantity() - orderQuantityList[i]);
			productsColorRepository.save(currentProduct);
		}

		Optional<ProductsColor> newPro = productsColorRepository.findById((long) 1);
		newPro.get().setQuantity(newPro.get().getQuantity() + 1);
		productsColorRepository.save(newPro.get());

		addOrder.setAllPrice(allPrice);
		addOrder = ordersRepository.save(addOrder);
		return addOrder;
	}

	// [ Change Order Status ]
	public ActionResponseModel changeOrderStatus(int statusId, long orderId, HttpServletRequest request) {
		UsernamesModels currentUserName = usernameRepository.findByUserName(TokenUtills.getUserNameFromToken(request));

		OrderStatus status = orderStatusRepository.findById(statusId)
				.orElseThrow(() -> new ExceptionFoundation(EXCEPTION_CODES.SEARCH_NOT_FOUND,
						"[ NOT FOUND ] This status with this ID is nit exist."));

		Orders currentorder = ordersRepository.findById(orderId)
				.orElseThrow(() -> new ExceptionFoundation(EXCEPTION_CODES.SEARCH_NOT_FOUND,
						"[ NOT FOUND ] Order with this ID is nit exist."));

		if (currentorder.getUserNameID() != currentUserName.getUserNameID()
				|| currentUserName.getRole() != rolesRepository.findById(2).get()) {
			throw new ExceptionFoundation(EXCEPTION_CODES.SAVE_NOT_THE_OWNER,
					"[ NOT ALLOWED ] This order is not belong to you or you are not an admin.");
		}
		if (currentorder.getOrderStatus() != orderStatusRepository.findById(1).get()) {
			throw new ExceptionFoundation(EXCEPTION_CODES.SHOP_NOT_ALLOW_TO_CANCLE,
					"[ PAID ] You paid for this product or is not in a status that you will be able to cancle.");
		}

		currentorder.setOrderStatus(status);
		ordersRepository.save(currentorder);
		return new ActionResponseModel("Change status to " + status.getStatus(), true);
	}

	// [Delete order]
	private void deleteOrder(long id) {
		ordersRepository.deleteById(id);
	}

}

/*
 * Status List [ 1 ] To Pay [ 2 ] To Ship [ 3 ] To Receive [ 4 ] Completed [ 5 ]
 * Cancelled
 */