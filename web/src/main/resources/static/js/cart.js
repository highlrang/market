$(document).ready(function(){
    $("#check_all").click(function(){
        if($("input:checkbox[id='check_all']").prop("checked")){
            $("input[type=checkbox]").prop("checked", true);
        }else{
            $("input[type=checkbox]").prop("checked", false);
        }
    });

    $("#order").click(function(){
        var item_id_list = [];
        $("input:checkbox[name='item_id']:checked").each(function(){
            item_id_list.push($(this).val());
        });

        if(item_id_list.length == 0){
            alert("상품을 선택해주세요.");
            return false;
        }

        var data = {
            customer_id: $("#customer_id").val(),
            cart_id: $("#cart_id").val(),
            item_id: item_id_list
        };

        var form = document.createElement('form');
        form.setAttribute('method', 'post');
        form.setAttribute('action', '/cart/order/ready');
        document.charset = "utf-8";
        for (var key in data) {
            var hiddenField = document.createElement('input');
            hiddenField.setAttribute('type', 'hidden');
            hiddenField.setAttribute('name', key);
            hiddenField.setAttribute('value', data[key]);
            form.appendChild(hiddenField);
        }
        document.body.appendChild(form);
        form.submit();

    });

    $("#cart-delete").click(function(){
        var item_id_list = [];
        $("input:checkbox[name='item_id']:checked").each(function(){
            item_id_list.push($(this).val());
        });

        if(item_id_list.length == 0){
            alert("상품을 선택해주세요.");
            return false;
        }

        var data = {
            cart_id: $("#cart_id").val(),
            delete_item_id: item_id_list
            // order에 상품 id 사용하기에 cart-delete에도 cartItem id가 아닌 Item의 id 전달
        };

        var form = document.createElement('form');
        form.setAttribute('method', 'post');
        form.setAttribute('action', '/cart/remove');
        document.charset = "utf-8";
        for (var key in data) {
            var hiddenField = document.createElement('input');
            hiddenField.setAttribute('type', 'hidden');
            hiddenField.setAttribute('name', key);
            hiddenField.setAttribute('value', data[key]);
            form.appendChild(hiddenField);
        }
        document.body.appendChild(form);
        form.submit();

    });
});