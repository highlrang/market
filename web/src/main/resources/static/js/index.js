$(function(){
    /* <![CDATA[ */
    var msg = /*[[ ${msg} ]]*/;
 	/* ]]> */
    if(msg != null){
        alert(msg);
    }

    $("input[type='button']").attr('class', 'btn btn-light');
    $("input[type='submit']").attr('class', 'btn btn-light');
    $("a").attr('class', 'btn btn-light');

});