function orderListChange(data){
    console.log(data);

    var orderList = "<div class='list-group'>";

    $.each(data["list"], function(index, order){
        orderList += "<a href=\'/order/detail/" + order.id + "\' class=\'list-group-item list-group-item-action\'>"
                  + "<div class='d-flex w-100 justify-content-between'>"
                  + "<h5 class='mb-1'>" + order.orderItemsName + "</h5>"
                  + "<small>" + order.orderDate + "</small></div>"
                  + "<p class='mb-1'>" + order.totalPrice + "원</p>";

        if(order.orderStatus == '주문 취소'){
            orderList += "<small>" + order.orderStatus + "</small>";
        }else{
            orderList += "<small>" + order.deliveryStatus + "</small>";
        }
        orderList += "</a>";
    });

    orderList += "</div>";

    $("#orderList").empty();
    $("#orderList").append(orderList);
}

$(function(){
    $("#size").change(function(){
        var size = $(this).val();
        var data = {
            page: "1",
            size: size
        };

        $.ajax({
            type: 'get',
            url: '/order/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val("1");
                $("#total_page").val(data["totalPage"]);

                orderListChange(data);
                pagination("1", size, data["totalPage"]);
            },
            error : function(request, status, error) {
                console.log(request, status, error);
            }
        });
    });
});


$(function(){
    $(document).on("click", "input[name='page']", function(){
        $("input[name='page']").attr('class', 'btn btn-light');
        $(this).attr('class', 'btn btn-dark');

        var page = $(this).val();
        var data = {
            page: page,
            size: $("#size").val()
        };

        $.ajax({
            type: 'get',
            url: '/order/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val(page);

                orderListChange(data);
            },
            error : function(request, status, error) {
                console.log(request, status, error);
            }
        });
    });
});