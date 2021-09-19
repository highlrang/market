function pagination(first_page, size, total_page){ // 현재 페이지는 쿼리 페이지+1로 넘어옴
    var pages = "";
    if(first_page > 1){
        pages += "<input type='button' class='btn btn-light' name='previous' value='이전'>";
    }

    total_page = parseInt(total_page) + 1; // 마지막 페이지 포함이기에 +1
    var end = parseInt(first_page + 10);
    if(end > total_page) {
        end = total_page;
    }

    for(var i=first_page; i<end; i++){
        pages += "<input type='button' name='page' value='" + i + "'";
        if(i == now_page){
            pages += " class='btn btn-dark'";
        }else{
            pages += " class='btn btn-light'";
        }
        pages += ">";
    }

    if(end != total_page) {
        pages += "<input type='button' class='btn btn-light name='next' value='다음'>";
    }

    $("#pagination").empty();
    $("#pagination").append(pages);
}

function itemListChange(data){
    var itemList = "";
    $.each(data["list"], function(index, item){
        itemList += "<a href='/seller/item/detail/" + item.id + "' class='list-group-item list-group-item-action'>"
                 + "<div class='d-flex w-100 justify-content-between'>"
                 + "<h5 class='mb-1'>" + item.name + "원</h5></div>"
                 + "<p class='mb-1'>" + item.price + "원</p>"
                 + "<p class='mb-1'>" + item.stock + "개 남음</p>"
                 + "</a>";
    });

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

$(function(){
    $(document).on("click", "input[name='previous']", function(){
        var first_page = parseInt($("input[name='page']").first().val()) - 10;
        pagination(first_page, $("size").val(), $("total_page").val());

        $("input[name='page']").last().click();
    });

    $(document).on("click", "input[name='next']", function(){
        var first_page = parseInt($("input[name='page']").last().val()) + 1;
        pagination(first_page, $("#size").val(), $("#total_page").val());

        $("input[name='page']").first().click();
    });
});