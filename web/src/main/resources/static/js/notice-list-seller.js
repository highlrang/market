function noticeListChange(data){
    var noticeList = "<div class='mx-auto' style='width: 50%; height: 50%;'>"; // height 빼기?

    $.each(data["list"], function(index, notice){
        if(notice.confirm == true){
            noticeList += "<div class='alert alert-secondary' role='alert'>"
                       + "<h6 class='alert-heading' style='font-weight: bold'>" + notice.title + "</h6>"
                       + "<p>" + notice.content + "</p>"
                       + "<hr>"
                       + "<form action='/seller/notice/remove' method='post' onsubmit=\'return confirm(\"삭제하시겠습니까?\");\'>"
                       + "<input type='hidden' name='id' value='" + notice.id + "'>"
                       + "<input type='submit' value='삭제'>"
                       + "</form></div>";

        }else{
            noticeList += "<div class='alert alert-info' role='alert'>"
                       + "<h6 class='alert-heading' style='font-weight: bold'>" + notice.title + "</h6>"
                       + "<p>" + notice.content + "</p>"
                       + "<hr>"
                       + "<form action='/seller/notice/check' method='post'>"
                       + "<input type='hidden' name='id' value='" + notice.id + "'>"
                       + "<input type='hidden' name='seller_id' value='" + notice.seller.id + "'>"
                       + "<input type='submit' value='확인 완료'>"
                       + "</form></div>";
        }
    });

    noticeList += "</div>";

    $("#noticeList").empty();
    $("#noticeList").append(noticeList);
}

$(function(){
    $("#size").change(function(){
        var data = {
            page: "1",
            size: $(this).val()
        };

        $.ajax({
            type: 'get',
            url: '/seller/notice/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val("1");
                $("#total_page").val(data["totalPage"]);

                noticeListChange(data);
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

        var data = {
            page: $(this).val(),
            size: $("#size").val()
        };

        $.ajax({
            type: 'get',
            url: '/seller/notice/list/api',
            data: data,
            success: function(data) {
                console.log(data);
                $("#now_page").val(page);

                noticeListChange(data);
            },
            error : function(request, status, error) {
                console.log(request, status, error);
            }
        });

    });
});