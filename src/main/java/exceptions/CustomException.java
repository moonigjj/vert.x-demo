/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package exceptions;

/**
 *
 * @author tangyue
 * @version $Id: AdminException.java, v 0.1 2018-02-28 15:25 tangyue Exp $$
 */
public class CustomException extends RuntimeException {

    public CustomException(String message){
        super(message);
    }

    public CustomException(Throwable throwable){
        super(throwable);
    }

    public CustomException(String message, Throwable throwable){
        super(message, throwable);
    }
}
