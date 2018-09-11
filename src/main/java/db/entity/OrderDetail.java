/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package db.entity;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 *
 * @author tangyue
 * @version $Id: OrderDetail.java, v 0.1 2018-05-29 17:07 tangyue Exp $$
 */
@Data
public class OrderDetail {

    private Long id;

    private String orderNum;

    private Long dishId;

    private String dishName;

    private Integer dishQuantity;

    private BigDecimal dishPrice;

    private BigDecimal dishDiscountPrice;

    private String dishIcon;

    private Date createTime;

    private Date updateTime;
}
