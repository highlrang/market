$(function(){
    $.ajax({
            type: 'get',
            url: '/api/category/list',
            dataType: 'application/json',
            success: function(data) {
                var categories = "";
                $.each(data, function(index, category){
                    categories += "<li><a class='dropdown-item' th:href='@{/item/list/{category}(category=" +
                    category.getKey() + ")}'>" + category.getValue() + "</a></li>";
                });

                $("#header-category").empty();
                $("#header-category").append(categories);
            },
            error : function(request, status, error) {
                console.log(request, status, error);
            }
        });

    $("input[type='button']").attr('class', 'btn btn-light');
    $("input[type='submit']").attr('class', 'btn btn-light');
    $("a").attr('class', 'btn btn-light');
});