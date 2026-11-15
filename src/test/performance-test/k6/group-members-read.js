import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';

import {
    Trend,
    Rate,
    Counter
} from 'k6/metrics';


/* =========================================================
   CONFIG
   ========================================================= */

const BASE_URL =
    __ENV.BASE_URL || 'http://localhost:8080';

const JWT = __ENV.JWT;

const OWNER_EMAIL =
    (__ENV.OWNER_EMAIL || '')
        .trim()
        .toLowerCase();

const TEST_USERS =
    (__ENV.TEST_USERS || '')
        .split(',')
        .map(v => v.trim().toLowerCase())
        .filter(Boolean);

const MEMBER_ROLE =
    __ENV.MEMBER_ROLE || 'MEMBER';

const SEED_GROUPS =
    Number(__ENV.SEED_GROUPS || 100);

const PAGE_SIZE =
    Number(__ENV.PAGE_SIZE || 4);


/* =========================================================
   CUSTOM METRICS
   ========================================================= */

const readDuration =
    new Trend('group_member_read_duration', true);

const readFailed =
    new Rate('group_member_read_failed');

const integrityRate =
    new Rate('group_member_response_integrity');

const paginationDuplicate =
    new Rate('pagination_duplicate_detected');

const status200 =
    new Counter('status_200');

const status401 =
    new Counter('status_401');

const status403 =
    new Counter('status_403');

const status404 =
    new Counter('status_404');

const status409 =
    new Counter('status_409');

const status5xx =
    new Counter('status_5xx');

const successfulReads =
    new Counter('successful_group_member_reads');


/* =========================================================
   LOAD TEST
   ========================================================= */
export const options = {

    summaryTrendStats: [
        'avg',
        'min',
        'med',
        'max',
        'p(90)',
        'p(95)',
        'p(99)'
    ],

    scenarios: {

        distributed_reads: {

            executor: 'ramping-arrival-rate',

            startRate: 50,

            timeUnit: '1s',

            preAllocatedVUs: 50,

            maxVUs: 500,

            stages: [
                { target: 100, duration: '30s' },
                { target: 250, duration: '45s' },
                { target: 500, duration: '1m' },
                { target: 750, duration: '1m' },
                { target: 0, duration: '20s' }
            ],

            exec: 'distributedRead'
        },


        hot_group_reads: {

            executor: 'constant-vus',

            vus: 100,

            duration: '1m',

            startTime: '4m',

            exec: 'hotGroupRead'
        },


        pagination_test: {

            executor: 'constant-arrival-rate',

            rate: 30,

            timeUnit: '1s',

            duration: '45s',

            preAllocatedVUs: 20,

            maxVUs: 100,

            startTime: '5m5s',

            exec: 'paginationRead'
        }
    },


    thresholds: {

        group_member_read_failed: [
            'rate<0.01'
        ],

        group_member_response_integrity: [
            'rate>0.99'
        ],

        group_member_read_duration: [
            'p(95)<200',
            'p(99)<500'
        ],

        pagination_duplicate_detected: [
            'rate<0.01'
        ],

        dropped_iterations: [
            'count==0'
        ]
    }
};


/* =========================================================
   HEADERS
   ========================================================= */

function requestParams(tags = {}) {

    return {

        headers: {

            Authorization:
                `Bearer ${JWT}`,

            'Content-Type':
                'application/json'
        },

        redirects: 0,

        tags: tags
    };
}


/* =========================================================
   GROUP CREATION PAYLOAD
   ========================================================= */

function createGroupPayload(groupName) {

    const uniqueEmails =
        [...new Set([
            OWNER_EMAIL,
            ...TEST_USERS
        ])]
            .filter(Boolean);


    const members =
        uniqueEmails.map(email => ({

            email: email,

            role: MEMBER_ROLE

        }));


    return JSON.stringify({

        groupName: groupName,

        members: members,

        file: null
    });
}


/* =========================================================
   EXTRACT PUBLIC GROUP ID
   ========================================================= */

function extractPublicGroupId(response) {

    try {

        const body = response.json();

        return (
            body?.groups?.groupPublicId
            ??
            body?.group?.publicGroupId
            ??
            body?.groupResponse?.publicGroupId
            ??
            body?.publicGroupId
            ??
            null
        );

    } catch (_) {

        return null;
    }
}

/* =========================================================
   SETUP

   Create test groups ONCE.

   Collect every publicGroupId.

   All VUs later reuse this returned data.
   ========================================================= */

export function setup() {

    if (!JWT) {

        throw new Error(
            'JWT environment variable is missing.'
        );
    }


    if (!OWNER_EMAIL) {

        throw new Error(
            'OWNER_EMAIL environment variable is missing.'
        );
    }


    const runId =
        `k6-read-${Date.now()}`;


    const groupIds = [];


    console.log(
        `Creating ${SEED_GROUPS} groups...`
    );


    for (
        let i = 0;
        i < SEED_GROUPS;
        i++
    ) {

        const groupName =
            `${runId}-group-${i}`;


        const response =
            http.post(

                `${BASE_URL}/api/drive/data/create-group`,

                createGroupPayload(groupName),

                requestParams({
                    phase: 'setup'
                })
            );


        const publicGroupId =
            extractPublicGroupId(response);


        const success =
            (
                response.status === 200
                ||
                response.status === 201
            )
            &&
            publicGroupId !== null;


        if (!success) {

            console.error(
                `SETUP FAILED | ` +
                `index=${i} | ` +
                `status=${response.status} | ` +
                `body=${response.body}`
            );

            continue;
        }


        groupIds.push(publicGroupId);
    }


    console.log(
        `Created ${groupIds.length}/${SEED_GROUPS} groups.`
    );


    if (groupIds.length === 0) {

        throw new Error(
            'No publicGroupIds were created.'
        );
    }


    console.log(
        `FIRST_GROUP_ID=${groupIds[0]}`
    );


    return {

        runId: runId,

        groupIds: groupIds,

        hotGroupId: groupIds[0]
    };
}


/* =========================================================
   EXACT URL MATCHING YOUR CONTROLLER
   ========================================================= */

function groupMembersUrl(
    groupId,
    page,
    pageSize
) {

    return (
        `${BASE_URL}` +
        `/api/drive/data/` +
        `${groupId}` +
        `/group-members` +
        `?page=${page}` +
        `&pageSize=${pageSize}`
    );
}


/* =========================================================
   STATUS COUNTER
   ========================================================= */

function trackStatus(status) {

    if (status === 200) {
        status200.add(1);
    }
    else if (status === 401) {
        status401.add(1);
    }
    else if (status === 403) {
        status403.add(1);
    }
    else if (status === 404) {
        status404.add(1);
    }
    else if (status === 409) {
        status409.add(1);
    }
    else if (status >= 500) {
        status5xx.add(1);
    }
}


/* =========================================================
   READ MEMBERS
   ========================================================= */

function readMembers(
    groupId,
    page,
    pageSize,
    testType
) {

    const url =
        groupMembersUrl(
            groupId,
            page,
            pageSize
        );


    const response =
        http.get(

            url,

            requestParams({

                endpoint:
                    'group-members',

                testType:
                    testType
            })
        );


    trackStatus(
        response.status
    );


    readDuration.add(
        response.timings.duration,
        {
            testType:
                testType
        }
    );


    /*
     * DEBUG ONLY FIRST FEW FAILURES.
     *
     * This prevents 500,000 error lines.
     */
    if (
        response.status !== 200
        &&
        __VU <= 2
        &&
        __ITER < 3
    ) {

        console.error(
            `READ FAILED | ` +
            `status=${response.status} | ` +
            `group=${groupId} | ` +
            `page=${page} | ` +
            `url=${url} | ` +
            `body=${response.body}`
        );
    }


    let body = null;


    try {

        body = response.json();

    }
    catch (_) {

        body = null;
    }


    /*
     * Expected response:
     *
     * {
     *   message: "Success",
     *   groupMembers: {
     *      content: [...]
     *   }
     * }
     */

    const content =
        body?.groupMembers?.content;


    const statusGood =
        response.status === 200;


    const messageGood =
        body?.message === 'Success';


    const contentGood =
        Array.isArray(content);


    const pageSizeGood =
        contentGood
        &&
        content.length <= pageSize;


    const valid =
        statusGood
        &&
        messageGood
        &&
        contentGood
        &&
        pageSizeGood;


    readFailed.add(
        !valid,
        {
            testType:
                testType
        }
    );


    integrityRate.add(
        valid,
        {
            testType:
                testType
        }
    );


    if (valid) {

        successfulReads.add(
            1,
            {
                testType:
                    testType
            }
        );
    }


    check(
        response,
        {

            'HTTP 200':
                r =>
                    r.status === 200,

            'message Success':
                () =>
                    messageGood,

            'groupMembers.content exists':
                () =>
                    contentGood,

            'page size respected':
                () =>
                    pageSizeGood
        }
    );


    return {

        response:
            response,

        body:
            body,

        content:
            contentGood
                ? content
                : []
    };
}


/* =========================================================
   SCENARIO 1

   DISTRIBUTED READS
   ========================================================= */

export function distributedRead(data) {

    const iteration =
        exec.scenario.iterationInTest;


    const groupIndex =
        iteration
        %
        data.groupIds.length;


    const groupId =
        data.groupIds[groupIndex];


    /*
     * Most reads use page 0.
     *
     * Every few requests also test later pages.
     */

    const page =
        iteration % 5 === 0
            ? 1
            : 0;


    readMembers(

        groupId,

        page,

        PAGE_SIZE,

        'distributed'
    );
}


/* =========================================================
   SCENARIO 2

   HOT GROUP

   100 VUs repeatedly read SAME group.
   ========================================================= */

export function hotGroupRead(data) {

    readMembers(

        data.hotGroupId,

        0,

        PAGE_SIZE,

        'hot-group'
    );
}


/* =========================================================
   EXTRACT EMAIL
   ========================================================= */

function extractEmail(member) {

    if (!member) {

        return null;
    }


    return (
        member.email
        ??
        member.emailId
        ??
        member.userEmail
        ??
        null
    );
}


/* =========================================================
   SCENARIO 3

   PAGINATION

   Reads page 0 and page 1.

   Verifies same member does not appear in both pages.
   ========================================================= */

export function paginationRead(data) {

    const iteration =
        exec.scenario.iterationInTest;


    const groupIndex =
        iteration
        %
        data.groupIds.length;


    const groupId =
        data.groupIds[groupIndex];


    const firstPage =
        readMembers(

            groupId,

            0,

            PAGE_SIZE,

            'pagination'
        );


    const secondPage =
        readMembers(

            groupId,

            1,

            PAGE_SIZE,

            'pagination'
        );


    /*
     * Do not evaluate pagination integrity
     * when either request itself failed.
     */

    if (
        firstPage.response.status !== 200
        ||
        secondPage.response.status !== 200
    ) {

        return;
    }


    const page0Emails =
        new Set(

            firstPage.content

                .map(extractEmail)

                .filter(Boolean)
        );


    const page1Emails =
        secondPage.content

            .map(extractEmail)

            .filter(Boolean);


    const duplicate =
        page1Emails.some(
            email =>
                page0Emails.has(email)
        );


    paginationDuplicate.add(
        duplicate
    );


    check(
        duplicate,
        {

            'no member repeated across pages':
                value =>
                    value === false
        }
    );
}


/* =========================================================
   SUMMARY
   ========================================================= */

export function handleSummary(data) {

    const metric =
        name =>
            data.metrics[name]?.values || {};


    const summary = {

        test:
            'PencilDrive Group Member Read Test',

        performance: {

            requests:
                metric('http_reqs').count || 0,

            requestRate:
                metric('http_reqs').rate || 0,

            avgMs:
                metric('group_member_read_duration').avg || 0,

            medianMs:
                metric('group_member_read_duration').med || 0,

            p90Ms:
                metric('group_member_read_duration')['p(90)'] || 0,

            p95Ms:
                metric('group_member_read_duration')['p(95)'] || 0,

            p99Ms:
                metric('group_member_read_duration')['p(99)'] || 0,

            maxMs:
                metric('group_member_read_duration').max || 0
        },


        status: {

            http200:
                metric('status_200').count || 0,

            unauthorized401:
                metric('status_401').count || 0,

            forbidden403:
                metric('status_403').count || 0,

            notFound404:
                metric('status_404').count || 0,

            conflict409:
                metric('status_409').count || 0,

            server5xx:
                metric('status_5xx').count || 0
        },


        quality: {

            failureRate:
                metric('group_member_read_failed').rate || 0,

            integrityRate:
                metric('group_member_response_integrity').rate || 0,

            paginationDuplicateRate:
                metric('pagination_duplicate_detected').rate || 0,

            droppedIterations:
                metric('dropped_iterations').count || 0
        }
    };


    console.log(
        '\n===== PENCILDRIVE GROUP MEMBER READ TEST =====\n' +
        JSON.stringify(summary, null, 2) +
        '\n==============================================\n'
    );


    return {
        stdout: '\n'
    };
}


/* =========================================================
   TEARDOWN
   ========================================================= */

export function teardown(data) {

    console.log(
        `TEST COMPLETE | ` +
        `run=${data.runId} | ` +
        `groups=${data.groupIds.length}`
    );


    if (__ENV.PRINT_IDS === 'true') {

        console.log(
            `PUBLIC_GROUP_IDS=` +
            JSON.stringify(
                data.groupIds
            )
        );
    }
}