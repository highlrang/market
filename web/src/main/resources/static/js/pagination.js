function pagination(first_page, size, total_page){ // 현재 페이지는 쿼리 페이지+1
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
        if(i == $("#now_page").val()){
            pages += " class='btn btn-dark'";
        }else{
            pages += " class='btn btn-light'";
        }
        pages += ">";
    }

    if(end != total_page) {
        pages += "<input type='button' class='btn btn-light' name='next' value='다음'>";
    }

    $("#pagination").empty();
    $("#pagination").append(pages);
}

$(function(){
    $(document).on("click", "input[name='previous']", function(){
        var first_page = parseInt($("input[name='page']").first().val()) - 10;
        pagination(first_page, $("#size").val(), $("#total_page").val());

        $("input[name='page']").last().click();
    });

    $(document).on("click", "input[name='next']", function(){
        var first_page = parseInt($("input[name='page']").last().val()) + 1;
        pagination(first_page, $("#size").val(), $("#total_page").val());

        $("input[name='page']").first().click();
    });
});