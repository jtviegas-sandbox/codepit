---------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION custom_create_type(_type varchar(50), _type_definition varchar(200), _owner varchar(20)) 
returns integer
as 	
$BODY$
declare
	_rows integer;
	_result bit := 1;
begin

select count(pg_catalog.pg_type.typname) into _rows from pg_catalog.pg_type where pg_catalog.pg_type.typname=_type;

if _rows > 0 then
	execute 'drop type ' || _type || ' cascade';
end if;

execute 'create type ' || _type || ' as (' || _type_definition || ')' ;
execute 'alter type ' || _type || ' owner to ' || _owner;

_result := 0;

RETURN _result;	
end;
$BODY$
 language 'plpgsql';

COMMENT ON FUNCTION custom_create_type(_type varchar(50), _type_definition varchar(200), _owner varchar(20)) is 
'
Helper function for type creation, checking the existence of the desired type and deleting it,
cascading the delete onto the associated functions.
arguments:	
	_type			varchar(50)		type name
	_type_definition	varchar(200)		system types comma separated list
	_owner			varchar(20)		user to own the type
';
ALTER FUNCTION custom_create_type(_type varchar(50), _type_definition varchar(200), _owner varchar(20)) OWNER TO tplinux;




CREATE OR REPLACE FUNCTION custom_get_reportdata_salesperclientandplubetweendates(text) 
	returns setof custom_type_reportdata_salesperclientandplubetweendates
	as 	
	$BODY$
	declare
		r record;
		/**
		* remember conditions must be something like 
			* where headers_trailers.bdate='' and headers_trailers.custnmbr='' and items.plunmbr=''
		*/
		CONDITIONS alias for $1;
		result custom_type_reportdata_salesperclientandplubetweendates;		
		business_date_query varchar(100):='select * from custom_get_available_businessdates()';
		items_pre_query text:='select bdate, termnmbr, transnmbr, plunmbr, pludesc, deptnmbr, price1, qtySold, amtSold, sumamtdisc,itmz from ejitem';
		headers_pre_query text:='select bdate, termnmbr, transnmbr, custnmbr,saletype, functype from ejheader' ; 
		trailers_pre_query text:='select bdate, termnmbr, transnmbr from ejtrailer';
		headers_pre_query_clause text:=' where functype=0 and saletype in (0,5) ';
		items_query text:='';
		headers_query text:='';
		trailers_query text:='';
		query text;
	begin

	for r IN EXECUTE business_date_query LOOP

		if items_query = '' then
			items_query := items_pre_query || r.custom_get_available_businessdates;
		else
			items_query := items_query || ' union ' || items_pre_query || r.custom_get_available_businessdates;
		end if;

		if headers_query = '' then
			headers_query := headers_pre_query || r.custom_get_available_businessdates || headers_pre_query_clause;
		else
			headers_query := headers_query || ' union ' || headers_pre_query || r.custom_get_available_businessdates || headers_pre_query_clause;
		end if;

		if trailers_query = '' then
			trailers_query := trailers_pre_query || r.custom_get_available_businessdates;
		else
			trailers_query := trailers_query || ' union ' || trailers_pre_query || r.custom_get_available_businessdates;
		end if;

	end loop;

	query := 'select headers.bdate,headers.termnmbr,headers.transnmbr,headers.custnmbr from '
		|| '(' || headers_query || ')headers inner join (' || trailers_query || ')trailers '
		|| 'on headers.bdate=trailers.bdate and headers.termnmbr=trailers.termnmbr and headers.transnmbr=trailers.transnmbr';
	
	query:='select headers_trailers.bdate, headers_trailers.termnmbr, '
		 || 'headers_trailers.transnmbr , headers_trailers.custnmbr ,'
		|| 'items.deptnmbr,items.plunmbr,items.pludesc,'
		|| 'items.qtySold,items.amtSold ,items.sumamtdisc,items.itmz'
		|| ' from (' || query || ')headers_trailers inner join (' || items_query || ')items '
		|| ' on headers_trailers.bdate=items.bdate and headers_trailers.termnmbr=items.termnmbr '
		|| 'and headers_trailers.transnmbr=items.transnmbr ';

	query:='select q.bdate,q.termnmbr,q.transnmbr,q.custnmbr,customer.custname,q.deptnmbr,q.plunmbr,q.pludesc,q.qtySold,q.amtSold,q.sumamtdisc,q.itmz '
		|| 'from (' || query || ')q inner join customer on q.custnmbr=customer.custnmbr';
	
	query:='select b.bdate,b.termnmbr,b.transnmbr,b.custnmbr,b.custname,b.deptnmbr,dept.deptdesc,b.plunmbr,b.pludesc,b.qtySold,b.amtSold,b.sumamtdisc,b.itmz '
		|| 'from (' || query || ')b inner join dept on b.deptnmbr=dept.deptnmbr';
	
	query:='select c.bdate,c.termnmbr,c.transnmbr,c.custnmbr,c.custname,c.deptnmbr,c.deptdesc,c.plunmbr,c.pludesc,' 
		|| 'case when pluext.price1 is null then 0 else pluext.price1 end as buyprice,c.qtySold,c.amtSold,c.sumamtdisc,c.itmz '
		|| 'from (' || query || ')c left join pluext on c.plunmbr=pluext.plunmbr';
	
	query:='select a.bdate as bdate,a.custnmbr,a.custname,a.deptnmbr,a.deptdesc,a.plunmbr,a.pludesc,' 
		|| 'a.qtySold,a.amtSold,a.sumamtdisc,
		(a.buyprice::float * a.qtySold::float)::int8 as amtsoldcost,
		(a.amtSold::float-a.sumamtdisc::float)::int8 as netamtsold,
		case when a.amtSold=0 
			then 0 
			when a.qtySold=0
			then 0
			else ((a.amtSold::float-a.sumamtdisc::float)-(a.buyprice::float * a.qtySold::float))::int8
		end as marginval
		from (' || query || ')a';


	query:='select d.bdate, d.custnmbr, d.custname,d.plunmbr, d.pludesc, d.deptnmbr,d.deptdesc,
			sum(d.qtySold) as qtysold, sum(d.amtSold) as amtsold,
			 sum(d.sumamtdisc) as amtdisc,sum(d.netamtsold) as netamtsold, 
			sum(d.marginval) as amtmargin
			,case 
				when sum(d.netamtsold)<>0 then
					((sum(d.marginval)/sum(d.netamtsold))*100)::int8 
				when sum(d.netamtsold)=0 and sum(d.amtsoldcost)>0 then
					(-1*100)::int8
				else 
					(0)::int8
			end as percmargin from (' || query || ')d '
	|| CONDITIONS
	|| ' group by		
	d.bdate, d.custnmbr, d.custname,d.plunmbr, d.pludesc, d.deptnmbr,d.deptdesc
	order by
	d.bdate, d.custnmbr, d.custname,d.plunmbr, d.pludesc, d.deptnmbr,d.deptdesc';
	
	raise debug '###sql to be performed is###  %',query;	

	for r IN EXECUTE query LOOP
		result.bdate=r.bdate;
		result.custnmbr=r.custnmbr;
		result.custname=r.custname;
		result.plunmbr=r.plunmbr;
		result.pludesc=r.pludesc;
		result.deptnmbr=r.deptnmbr;
		result.deptdesc=r.deptdesc;
		result.qtysold=r.qtySold;
		result.amtsold=r.amtSold;
		result.amtdisc=r.amtdisc;
		result.netamtsold=r.netamtsold;
		result.amtmargin=r.amtmargin;
		result.percmargin=r.percmargin;
		return next result;
	end loop;

	RETURN;
	end;
	$BODY$
	 language 'plpgsql';

ALTER FUNCTION custom_get_reportdata_salesperclientandplubetweendates(text) OWNER TO tplinux;
COMMENT ON FUNCTION custom_get_reportdata_salesperclientandplubetweendates(text) is 
'
Helper function which builds a set of records reflecting all the items 
from the transactions refering to all the available dates in the database.
arguments:	
returns:
	a set of transaction item types.
';

select * from custom_get_reportdata_salesperclientandplubetweendates(
'where d.custnmbr=9999 and d.plunmbr=2804508000000');