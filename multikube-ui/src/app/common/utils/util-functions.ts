import {QueryParams, QueryRequest} from '../rest/types/requests/query-request';
import {forkJoin, map, mergeMap, Observable, of} from 'rxjs';
import {PaginatedResponse} from '../rest/types/responses/paginated-response';

export function resolveErrorMessage(error: any): string{
    return error?.error?.error || error?.message || error?.error?.message || error;
}

export function buildQueryParams(queryRequest: QueryRequest): QueryParams | any{
    const params: QueryParams = {
        page: queryRequest.page,
        size: queryRequest.pageSize,
    };

    if (queryRequest.filter) {
        params.filter = queryRequest.filter;
    }

    if (queryRequest.sort) {
        params.sort = `${queryRequest.sort.sortField},${queryRequest.sort.sortType.toLowerCase()}`;
    }

    return params;
}

export interface GridPropertyFilter {
    property: string;
    value: string;
}

export function buildRestGridFilter(gridFilters: GridPropertyFilter[] | any[] | undefined): string | undefined{
    if(gridFilters){
        let restFilter = "";
        gridFilters.forEach((gridFilter, index) => {
            restFilter += `${gridFilter.property}==${gridFilter.value}`;
            restFilter += (index != gridFilters.length - 1) ? "," : "";
        })

        return restFilter;
    }

    return undefined;
}

export function getAllElementsFromAllPages<T>(
    request: (queryRequest?: QueryRequest) => Observable<PaginatedResponse<T>>,
    initialQuery?: QueryRequest,
): Observable<T[]> {
    return request(initialQuery).pipe(
        mergeMap((response: PaginatedResponse<T>) => {
            const allPages = [of(response)];
            for (let page = 2; page <= response.totalPages; page++) {
                allPages.push(request({
                    ...initialQuery,
                    page: page,
                }));
            }

            return forkJoin(allPages);
        }),
        map((responses: PaginatedResponse<T>[]) => {
            const allElements: T[] = [];
            responses.forEach((response) => {
                allElements.push(...response.content);
            });

            return allElements;
        })
    );
}
