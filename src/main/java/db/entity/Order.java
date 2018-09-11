/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package db.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 *
 * @author tangyue
 * @version $Id: OrderForm.java, v 0.1 2018-05-29 14:24 tangyue Exp $$
 */
@Data
public class Order {

    private Long id;

    private String orderNum;

    private Long userId;

    private Long merchantId;

    private String deskNum;

    private String buyerName;

    private String phone = "";

    private String address = "";

    private Integer amount = 0;

    private BigDecimal price = new BigDecimal("0");

    private BigDecimal pay = new BigDecimal("0");

    private Integer saleStatus = 2;

    private Integer payMethod = 2;

    private Integer payStatus = 2;

    private Date createTime;

    private Date updateTime;

    private transient List<OrderDetail> details;
}
