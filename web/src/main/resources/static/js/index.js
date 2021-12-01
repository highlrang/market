function customer_category(data){
    var category = "";

    $.each(data, function(key, value){
        category += "<li><a class='dropdown-item' href='/item/list/"
                 + key + "'>" + value + "</a></li>";
    });

    $("#customer-category").empty();
    $("#customer-category").append(category);
}

function seller_category(data){
    var category = "";

    $.each(data, function(key, value){
        category += "<li><a class='dropdown-item' href='/seller/item/list/"
                 + key + "'>" + value + "</a></li>";
    });

    $("#seller-category").empty();
    $("#seller-category").append(category);
}

$(function(){
    $.ajax({
        type: 'get',
        url: '/api/category/list',
        // dataType: 'application/json',
        success: function(data) {
            if(data["title"] == "customer") {
                customer_category(data["categories"]);

            }else if(data["title"] == "seller"){
                seller_category(data["categories"]);

            }
        },
        error : function(request, status, error) {
            console.log(request, status, error);
        }
    });

    $("input[type='button']").attr('class', 'btn btn-light');
    $("input[type='submit']").attr('class', 'btn btn-light');
});