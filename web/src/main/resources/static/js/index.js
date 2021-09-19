$(function(){
    $.ajax({
            type: 'get',
            url: '/api/category/list',
            // dataType: 'application/json',
            success: function(data) {
                var customer = "";
                var seller = "";
                $.each(data, function(key, value){
                    customer += "<li><a class='dropdown-item' href='/item/list/" +
                    key + "'>" + value + "</a></li>";

                    seller += "<li><a class='dropdown-item' href='/seller/item/list/" +
                    key + "'>" + value + "</a></li>";
                });

                $("#customer-category").empty();
                $("#customer-category").append(customer);

                $("#seller-category").empty();
                $("#seller-category").append(seller);
            },
            error : function(request, status, error) {
                console.log(request, status, error);
            }
        });

    $("input[type='button']").attr('class', 'btn btn-light');
    $("input[type='submit']").attr('class', 'btn btn-light');
});