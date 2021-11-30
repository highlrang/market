function customer_category(data){
    var category = "";
    let id;

    $.each(data, function(key, value){
        if(value == "동물용품") {
            id = " id='expose_category'";
        }else{
            id = "";
        }
        category += "<li><a class='dropdown-item' href='/item/list/"
                 + key + "'" + id + ">" + value + "</a></li>";
    });

    $("#customer-category").empty();
    $("#customer-category").append(category);
    // $("#expose_category").get(0).click();
}

function seller_category(data){
    var category = "";
    let id;

    $.each(data, function(key, value){
        if(value == "동물용품") {
            id = " id='expose_category";
        }else{
            id = "";
        }
        category += "<li><a class='dropdown-item' href='/seller/item/list/"
                 + key + "'" + id + ">" + value + "</a></li>";
    });

    $("#seller-category").empty();
    $("#seller-category").append(category);
    // $("#expose_category").trigger("click");
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