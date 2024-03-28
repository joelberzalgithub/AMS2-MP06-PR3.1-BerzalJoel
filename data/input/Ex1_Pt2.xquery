declare option output:method "xml";
declare option output:indent "yes";

<users>{
  for $u in //users/row
  order by count(//posts/row[@OwnerUserId = $u/@Id]) descending
  return <row>{$u/@DisplayName, count(//posts/row[@OwnerUserId = $u/@Id])}</row>
}</users>
