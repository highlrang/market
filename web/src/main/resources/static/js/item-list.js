function pagination(first_page, size, total_page){ // 현재 페이지는 쿼리 페이지+1로 넘어옴
    var pages = "";
    if(first_page > 1){
        pages += "<input type='button' name='previous' value='이전'>";
    }

    total_page = parseInt(total_page) + 1; // 마지막 페이지 포함이기에 +1
    var end = parseInt(first_page + 10);
    if(end > total_page) {
        end = total_page;
    }

    for(var i=first_page; i<end; i++){
        pages += "<input type='button' name='page' value='" + i + "'";
        if(i == now_page){
            pages += " class='btn btn-black'";
        }
        pages += ">";
    }

    if(end != total_page) {
        pages += "<input type='button' name='next' value='다음'>";
    }

    $("#pagination").empty();
    $("#pagination").append(pages);
}

function itemListChange(data){
    var itemList = "";
    $.each(data["list"], function(index, item){
        itemList += "<div>"
                 + "<div><a href='/item/detail/" + item.id + "'>"  + item.name + "</a></div>"
                 + "<div>" + item.price + "</div>"
                 + "<div>" + item.stock + "</div>"
                 + "<div>";
        $.each(item.photos, function(index, photo){
            itemList += "<img src='../" + photo.name
                     + "' alt='" + photo.originName
                     + "' width='200' height='200'>";
        });
        itemList += "</div></div><br>";
    });

    $("#itemList").empty();
    $("#itemList").append(itemList);
}

$(function(){
    $("#size").change(function(){
        var data = {
            category: $("#category").val(),
            page: "1",
            size: $(this).val()
        };

        $.ajax({
            type: 'get',
            url: '/item/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val(data["nowPage"]);
                $("#total_page").val(data["totalPage"]);

                itemListChange(data);

                pagination(data["nowPage"], data["nowSize"], data["totalPage"]);
            },
            error : function(request, status, error) {
                console.log(request, status, error);
            }
        });

    });
});

$(function(){
    $(document).on("click", "input[name='page']", function(){
        $("input[name='page']").attr('class', '');
        $(this).attr('class', 'btn btn-black');

        var data = {
            category: $("#category").val(),
            page: $(this).val(),
            size: $("#size").val()
        };

        $.ajax({
            type: 'get',
            url: '/item/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val(data["nowPage"]);

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