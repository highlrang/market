function itemListChange(data){
    var itemList = "<div class='list-group'>";
    $.each(data["list"], function(index, item){
        itemList += "<a href='/seller/item/detail/" + item.id + "' class='list-group-item list-group-item-action'>"
                 + "<div class='d-flex w-100 justify-content-between'>"
                 + "<h5 class='mb-1'>" + item.name + "</h5></div>"
                 + "<p class='mb-1'>" + item.price + "원</p>"
                 + "<p class='mb-1'>" + item.stock + "개 남음</p>"
                 + "</a>";
    });
    itemList += "</div>";
    $("#itemList").empty();
    $("#itemList").append(itemList);
}

$(function(){
    $("#size").change(function(){
        var size = $(this).val();
        var data = {
            category: $("#category").val(),
            page: "1",
            size: size
        };

        $.ajax({
            type: 'get',
            url: '/item/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val("1");
                $("#total_page").val(data["totalPage"]);

                itemListChange(data);

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
            category: $("#category").val(),
            page: page,
            size: $("#size").val()
        };

        $.ajax({
            type: 'get',
            url: '/item/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val(page);

                itemListChange(data);
            },
            error : function(request, status, error) {
                console.log(request, status, error);
            }
        });

    });
});